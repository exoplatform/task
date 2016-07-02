(function($) {
	var createTaskUrl = '';
	$(".create-task-button")
			.on(
					"click",
					function() {
						var username = $("#task-add-user").val();
						var task = $("#task-add-task").val();
						var dueDate = $("#task-add-date").val();
						var roomName = chatApplication.targetFullname;
						var isSpace = false;
						var roomId = chatApplication.targetUser;
						var targetUser = chatApplication.targetUser;
						if (targetUser.indexOf("space-") > -1) {
							isSpace = true;
						}

						// Validate empty
						if (task === $("#task-add-task").attr("data-value")
								|| task === "" || dueDate === "") {
							return;
						}

						// Validate datetime
						if (!uiMiniCalendar.isDate(dueDate)) {
							bootbox
									.alertError(
											chatBundleData["exoplatform.chat.date.invalid.message"],
											function(e) {
												e.stopPropagation();
												$("#task-add-date").select();
											});
							return;
						}

						// Get selected users
						var selectedUsers = "";
						var selectedFullNames = "";
						$(".task-user-label").each(function(index) {
							var name = $(this).attr("data-name");
							var fullname = $(this).attr("data-fullname");
							if (index === 0) {
								selectedUsers = name;
								selectedFullNames = fullname;
							} else {
								selectedUsers += "," + name;
								selectedFullNames += ", " + fullname;
							}
						});

						if (selectedUsers === "") {
							if (username !== "") {
								bootbox
										.alertError(
												chatBundleData["exoplatform.chat.task.invalidUser.message"]
														.replace("{0}",
																username),
												function(e) {
													e.stopPropagation();
													$("#task-add-user")
															.select();
												});
							}
							return;
						}
						hideMeetingPanel();
						// Disable button while server updating
						setActionButtonEnabled('.create-task-button', false);

						// Call server
						$.ajax({
							url : createTaskUrl,
							data : {
								"extension_action" : "createTask",
								"username" : selectedUsers,
								"dueDate" : dueDate,
								"task" : task,
								"roomName" : roomName,
								"isSpace" : isSpace
							},
							success : function(response) {

								var options = {
									type : "type-task",
									username : selectedUsers,
									fullname : selectedFullNames,
									dueDate : dueDate,
									task : task
								};
								var msg = task;

								chatApplication.chatRoom.sendMessage(msg,
										options, "true");
								setActionButtonEnabled('.create-task-button',
										true);

							},
							error : function(xhr, status, error) {
								console.log("error");
								setActionButtonEnabled('.create-task-button',
										true);
							}
						});
					});

	var sendMessage = ChatApplication.prototype.sendMessage; 
	ChatApplication.prototype.sendMessage = function(msg, callback) {
		var pattern = /\s*\+\+\S+/;
		if (pattern.test(msg)) {
			// Call server
			$.ajax({
				url : createTaskUrl,
				data : {
					"extension_action" : "createTaskInline",
					"msg": msg
				},
				success : function(response) {
					console.log("create inline task successful: " + response);
				},
				error : function(xhr, status, error) {
					console.log("fail to create inline task: " + error);
				}
			});
		}
		sendMessage.apply(this, arguments);
	}
	
	var getActionMeetingStyleClasses = ChatRoom.prototype.getActionMeetingStyleClasses;
	ChatRoom.prototype.getActionMeetingStyleClasses = function(options) {
		var actionType = options.type;
		var out = "";

		if ("type-task" === actionType) {
			out += "                <div class='msUserAvatar'>";
			out += "                <i class='uiIconChat32x32Task uiIconChat32x32LightGray'></i>";
			out += "                </div>";
		} else {
			return getActionMeetingStyleClasses.apply(this, arguments);
		}
	}

	var messageBeautifier = ChatRoom.prototype.messageBeautifier;
	ChatRoom.prototype.messageBeautifier = function(objMessage, options) {
		if (options.type === "type-task") {
			var out = "";
			out += "<b>" + options.task + "</b>";
			out += "<div class='msTimeEvent'>";
			out += "  <div>";
			out += "    <i class='uiIconChatAssign uiIconChatLightGray mgR10'></i><span class='muted'>"
					+ chatBundleData["exoplatform.chat.assign.to"]
					+ ": </span>" + options.fullname;
			out += "  </div>";
			out += "  <div>";
			out += "    <i class='uiIconChatClock uiIconChatLightGray mgR10'></i><span class='muted'>"
					+ chatBundleData["exoplatform.chat.due.date"]
					+ ":</span> <b>" + options.dueDate + "</b>";
			out += "  </div>";
			out += "</div>";
		} else {
			return messageBeautifier.apply(this, arguments);
		}
	}

	function hideMeetingPanel() {
		$(".meeting-action-popup").css("display", "none");
		$(".meeting-action-toggle").removeClass("active");
	}

	function setActionButtonEnabled(btnClass, isEnabled) {
		if (isEnabled) {
			$(btnClass).css('cursor', "default");
			$(btnClass).removeAttr('disabled');
		} else {
			$(btnClass).css('cursor', "progress");
			$(btnClass).attr('disabled', 'disabled');
		}
	}

	return {
		setActionUrl : function(url) {
			createTaskUrl = url;
		}
	}
})($);