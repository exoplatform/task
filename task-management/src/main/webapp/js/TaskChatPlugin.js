(function($) {
  var createTaskUrl = '';
    $('#task-add-user').suggester({
          type : 'tag',
          plugins: ['remove_button'],
          valueField: 'name',
          labelField: 'fullname',
          searchField: ['fullname'],
          sourceProviders: ['exo:task-add-user'],
          providers: {
            'exo:task-add-user': function(query, callback) {
                $.ajax({
                    type: "GET",
                    url: chatApplication.jzUsers,
                    data: {
                        filter : query,
                        user : chatApplication.username,
                        token : chatApplication.token,
                        dbName : chatApplication.dbName
                    },
                    complete: function(jqXHR) {
                        if(jqXHR.readyState === 4) {
                            var json = $.parseJSON(jqXHR.responseText)
                            if (json.users != null) {
                                callback(json.users);
                            }
                        }
                    }
                });
            } 
          }
      });

  $(".create-task-button").on("click", function() {
    var selectedUsers = $("#task-add-user").val();
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
  
  chatApplication.initMention();

  // TODO: Need to make sure this is executed before ChatRoom is initialized.
  chatApplication.registerEvent({
    'beforeSend' : function(context) {
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
            + ": </span>" + options.username;
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
  
  function setMiniCalendarToDateField(dateFieldId) {
    var dateField = document.getElementById(dateFieldId);
    dateField.onfocus=function(){
      uiMiniCalendar.init(this,false,"MM/dd/yyyy","", chatBundleData["exoplatform.chat.monthNames"]);
    };
    dateField.onkeyup=function(){
      uiMiniCalendar.show();
    };
    dateField.onkeydown=function(event){
      uiMiniCalendar.onTabOut(event);
    };
    dateField.onclick=function(event){
      event.cancelBubble = true;
    };
  };
  setMiniCalendarToDateField('task-add-date');

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