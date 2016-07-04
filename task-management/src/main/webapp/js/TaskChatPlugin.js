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
						var isTeam = false;
						var targetUser = chatApplication.targetUser;
						if (targetUser.indexOf("space-") > -1) {
							isSpace = true;
						} else if (targetUser.indexOf("team-") > -1) {
							isTeam = true;
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
								"isSpace" : isSpace,
								"isTeam": isTeam
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
	
	var $msg = $("#msg");
	var $autoComplete = $('<div class="uiAutoComplete" style="display: none; top: 57px; left: 44px"></div>');
	$msg.after($autoComplete);
	
	var searching = false;
	var currMsg = '';
	var currSymbolIdx = -1;
	chatApplication.registerPlugin({
		'getEvent' : function() {
			return 'keyUp';
		},	
		'onEvent' : function(context) {
			var event = context.event;
			if (searching) {
				if ( event.which === 13|| event.which === 27 ) {
					finish(event.which === 13);
					context.continueSend = false;
				} else if (event.which === 40 || event.which === 38) {
					var isUp = (event.which === 38);
					
					var $next = null;
					var $selected = $autoComplete.find('.selected');
					if (isUp) {
						$next = $selected.next('.item');
						if (!$next.length) {
							$next = $autoComplete.find('.item').first();
						}						
					} else {
						$next = $selected.prev('.item');
						if (!$next.length) {
							$next = $autoComplete.find('.item').last();
						}
					}
					
					$selected.removeClass('selected');
					$next.addClass('selected');
					
					context.continueSend = false;
				} else {
					//searching
					var idx = findNearestSymbol(event);
					if (idx != -1) {
						showAutoComplete(idx);
					} else {
						finish(false);
					}
				}			
			} else {
				var idx = findNearestSymbol(event);
				if (idx != -1) {
					showAutoComplete(idx);
				}
			}
			currMsg = $msg.val();
		}
	});
	
	function finish(completed) {
		searching = false;
		if (completed) {
			var $selected = $autoComplete.find('.selected');
			applyUser($selected.html());
		}
		hideAutoComplete();
	}
	
	function showAutoComplete(idx) {
		searching = true;
		currSymbolIdx = idx;
		var query = findSearchTerm(currSymbolIdx);
		
		if (query.length != 0) {
			chatApplication.getAllUsers(query, function(response) {
				if (response && response.users) {
					var res = '';
					$.each(response.users, function(idx, user) {
						res += '<div class="item">' + user.name + '</div>';
					});
					$autoComplete.html(res);
					$autoComplete.find('.item').first().addClass('selected');
				}
			});
			$autoComplete.show();			
		}
	}
	
	function applyUser(username) {
		if (currSymbolIdx != -1) {
			var msg = $msg.val();
			
			var end = -1;
			for (var i = currSymbolIdx; i < msg.length; i++) {
				var code = msg.charCodeAt(i); 
				if (code < 33 || code > 126) {
					end = i;
					break;
				}
			}
						
			var tmp = msg.slice(0, currSymbolIdx + 1) + username;
			if (end != -1) {
				tmp += msg.slice(end, -1);
			}
			$msg.val(tmp);
		}
	}
	
	function findNearestSymbol(event) {
		var idxDiff = -1;
		var idx = -1;
		var msg = $msg.val();

		if (msg.length > 0) {
			if (msg !== currMsg) {
				for (var i = 0; i <= msg.length; i++) {
					idxDiff = i;
					if (i == msg.length || i == currMsg.length || msg.charCodeAt(i) != currMsg.charCodeAt(i)) {
						break;
					}
				}			
			}
			
			if (idxDiff != -1) {
				for (var i = idxDiff; i >= 0; i--) {
					var code = msg.charCodeAt(i); 					
					if (code == 64) {
						idx = i;
						break;
					} else if (code < 33 || code > 126) {
						break;
					}
				}
			}			
		}
		return idx;
	}
	
	function findSearchTerm(idx) {
		var msg = $msg.val();
		if (idx + 1 < msg.length) {
			var end = -1;
			for (var i = idx + 1; i < msg.length; i++) {
				var code = msg.charCodeAt(i); 
				if (code < 33 || code > 126) {
					end = i;
					break;
				}
			}
			if (end == -1 || idx +1 < end) {
				return msg.slice(idx + 1, end);							
			} else {
				return  '';
			}
		} else {
			return '';
		}
	}
	
	function hideAutoComplete() {
		$autoComplete.hide();		
		var $selected = $autoComplete.find('.selected');
		$selected.removeClass('selected');
	}
	
//	$('#msg').exoMentions({
//        onDataRequest : function (mode, query, callback) {
//            var _this = this;
//            $.ajax({
//            	url: chatApplication.jzUsers,
//            	data: {"filter": query,
//        	      "user": chatApplication.username,
//        	      "token": chatApplication.token,
//        	      "dbName": chatApplication.dbName
//        	    },
//        	    dataType: "json",
//        	    success: function(data) {
//        	    	var users = [];
//        	    	$.each(data.users, function(idx, user) {
//        	    		users.push({
//        	    			"id": user.name,
//        	    			"name": user.fullname,
//        	    			"type": "contact"
//        	    		});
//        	    	});
//                    callback.call(_this, users);
//                }
//            });
//        }
//    });
	
	chatApplication.registerPlugin({
		'getEvent' : function() {
			return 'beforeSend';
		},	
		'onEvent' : function(context) {
			var msg = context.msg;
			
			var pattern = /\s*\+\+\S+/;
			if (pattern.test(msg)) {
				context.continueSend = false;
				var msg = msg.replace("++", "");
				var roomName = chatApplication.targetFullname;
				var isSpace = false, isTeam = false;
				var targetUser = chatApplication.targetUser;
				if (targetUser.indexOf("space-") > -1) {
					isSpace = true;
				} else if (targetUser.indexOf("team-") > -1) {
					isTeam = true;
				}
				
				chatApplication.getUsers(targetUser, function (jsonData) {
					var participants = [];
					$.each(jsonData.users, function(idx, elem) {
						participants.push(elem.name);					
					});
					
					// Call server
					$.ajax({
						url : createTaskUrl,
						data : {
							"extension_action" : "createTaskInline",
							"task": msg,
							"isSpace": isSpace,
							"isTeam": isTeam,
							"roomName": roomName,
							"participants": participants.join(",")
						},
						success : function(response) {
							var options = {
									"type" : "type-task",
									"username" : response.assignee,
									"fullname" : response.fullName,
									"task" : response.title,
									"dueDate" : response.dueDate
							};
							
							chatApplication.chatRoom.sendMessage(response.title,
									options, "true");
							setActionButtonEnabled('.create-task-button',
									true);
						},
						error : function(xhr, status, error) {
							console.log("fail to create inline task: " + error);
						}
					});				
				});			
			}
		}
	});
	
	chatApplication.chatRoom.registerPlugin({
		"getType" : function() {
			return "type-task";
		},
		"getActionMeetingStyleClasses" : function(options) {
			var actionType = options.type;
			var out = "";

			if ("type-task" === actionType) {
				out += "                <i class='uiIconChat32x32Task uiIconChat32x32LightGray'></i>";
			}
			return out;
		},
		"messageBeautifier" : function(objMessage, options) {
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
				return out;
			}
		}
	});

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