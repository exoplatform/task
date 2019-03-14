require(['SHARED/extensionRegistry'], function(extensionRegistry) {
  extensionRegistry.registerExtension('chat', 'composer-application', chatTaskComposerAppPlugin);
  extensionRegistry.registerExtension('chat', 'message-type', chatTaskMessageType);
  extensionRegistry.registerExtension('chat', 'message-notif', chatTaskNotification);
});

var chatTaskNotification = {
  key: 'task-notif',
  type: 'type-task',
  iconClass: 'uiIconChatCreateTask uiIconChatLightGray pull-left',
  html: function(notif, i18N) {
    return notif.options ? notif.options.task : '';
  }
};

var chatTaskMessageType = {
  key: 'task-message',
  type: 'type-task',
  iconClass: 'uiIconChatCreateTask',
  html: function(message, i18N) {
    if (!message || !message.options || !i18N) {
      return '';
    }
    return '<b><a href="' + message.options.url + '" target="_blank">' + message.options.task + '</a></b> \
    <div class="custom-message-item"> \
      <span><i class="uiIconChatAssign"></i> \
        ' + i18N('exoplatform.chat.assign.to') + ': \
      </span> \
      <b>' + (message.options.username || i18N('exoplatform.chat.assign.to.none')) + ' </b> \
    </div> \
    <div class="custom-message-item"> \
      <span> \
        <i class="uiIconChatClock"></i> \
        ' + i18N('exoplatform.chat.due.date') + ':  \
      </span> \
      <b> \
      ' + (message.options.dueDate || i18N('exoplatform.chat.due.date.none')) + ' \
      </b> \
    </div>';
  }
};

var chatTaskComposerAppPlugin = {
  key: 'task',
  rank: 60,
  type: 'type-task',
  notificationContent: function(msg) {
    return msg.options.task;
  },
  shortcutMatches: function(msg) {
    return /\s*\+\+\S+/.test(msg);
  },
  shortcutCallback: function(chatServices, $, msg, contact) {
    this.$ = $;
    var thiss = this;
    var username = null;
    if (msg.indexOf(' @') > -1) {
      var messages = msg.split(' @');
      username = messages[1];
    }
    var message = {
      msg : '',
      room : contact.room,
      clientId: new Date().getTime().toString(),
      user: eXo.chat.userSettings.username,
      isSystem: true,
      options: {
        type: 'type-task',
        username: username,
        fromUser: eXo.chat.userSettings.username,
        fromFullname: eXo.chat.userSettings.fullName
      }
    };
    var isSpace = contact.user.indexOf('space-') === 0;
    var isTeam = contact.user.indexOf('team-') === 0;
    var roomName = contact.fullName;
    if(isSpace) {
      // FIXME very ugly, the technical ID should be used here instead
      roomName = roomName.toLowerCase().split(' ').join('_');
    }
    var data = {
      'extension_action' : 'createTaskInline',
      'text' : msg,
      'roomName' : roomName,
      'isSpace' : isSpace,
      'isTeam': isTeam,
      'participants': isSpace || isTeam ? contact.participants.join(',') : contact.user
    };
    this.saveTask(eXo.chat.userSettings, data).then(function(response) {return response.text()}).then(function(data) {
      data = JSON.parse(data);
      thiss.processData(data, message);

      document.dispatchEvent(new CustomEvent('exo-chat-message-tosend-requested', {'detail' : message}));
    });
  },
  nameKey: 'exoplatform.chat.task',
  labelKey: 'exoplatform.chat.assign.task',
  iconClass: 'uiIconChatCreateTask',
  html: function(i18NConverter) {
    return '<input id="taskTitle" name="text" class="large" type="text" placeholder="' + i18NConverter('exoplatform.chat.task.title') + '" required> \
            <input id="taskAssignee" name="username" class="large" type="text" placeholder="' + i18NConverter('exoplatform.chat.assignee') + '"> \
            <input id="taskDueDate" name="dueDate" format="MM/dd/yyyy" placeholder="' + i18NConverter('exoplatform.chat.due.date') + '" class="large" type="text" onfocus="require([\'SHARED/CalDateTimePicker\'], (CalDateTimePicker) => CalDateTimePicker.init(event.target, false));">';
  },
  mount: function($, chatServices) {
    var $msg = $('#messageComposerArea');
    if(!$msg.length) {
      return;
    }
    $msg.suggester({
      type : "mix",
      sourceProviders: ['exo:task-add-user-inline'],
      showAtCaret: true,
      valueField: 'name',
      labelField: 'fullname',
      searchField: ['fullname'],
      providers: {
        'exo:task-add-user-inline': function(query, callback) {
          if (!query || !query.trim().length) {
            return callback();
          }
          chatServices.getChatUsers(eXo.chat.userSettings, query.trim()).then(function(data) {
            if(data && data.users) {
              callback(data.users);
            }
          });
        }
      },
      renderMenuItem: function(item) {
        var avatar = chatServices.getUserAvatar(item.name);
        var defaultAvatar = '/chat/img/room-default.jpg';
        return '<img src="' + avatar + '" onerror="this.src=\'' + defaultAvatar + '\'" width="20px" height="20px"> ' +
        chatServices.escapeHtml(item.fullname) + ' <span style="float: right" class="chat-status-task chat-status-'+item.status+'"></span>';
      },
      renderItem: '@${name}',
      callbacks: {
        matcher: function(flag, subtext) {
          var pattern = /\s*\+\+\S+/;
          if (pattern.test(subtext)) {
            var pos = subtext.lastIndexOf(flag);
            if (pos > -1) {
              return subtext.substr(pos + 1);
            }
          }
          return null;
        }
      }
    });
  },
  htmlAdded: function($, chatServices) {
    this.$ = $;
    this.initSuggester($, chatServices);
  },
  submit: function(chatServices, message, formData, contact) {
    var thiss = this;
    var isSpace = contact.user.indexOf('space-') === 0;
    var isTeam = contact.user.indexOf('team-') === 0;
    var $taskAssignee = this.$('#taskAssignee');
    var $taskDueDate = this.$('#taskDueDate');
    var $taskTitle = this.$('#taskTitle');
    var data = {
      'extension_action' : 'createTask',
      'username' : $taskAssignee.suggester('getValue'),
      'dueDate' : $taskDueDate.val(),
      'text' : $taskTitle.val(),
      'roomName' : contact.fullName,
      'isSpace' : isSpace,
      'isTeam': isTeam,
      'participants': isSpace || isTeam ?
        contact.participants.map(function(participant) { return participant.name; }).join(',') : contact.user
    };
    return this.saveTask(eXo.chat.userSettings, data).then(function(response) {
      if (!response.ok) {
        return {errorCode : 'ErrorSaveTask'};
      }
      return response.json();
    }).then(function(data) {
      thiss.processData(data, message);
      return {ok : true};
    }).catch(function(e) {
      return {errorCode : 'ErrorSaveTask'};
    });
  },
  processData: function(data, message) {
    var url = data.url ? data.url : data.length && data.length === 1 && data[0].url ? data[0].url : '';
    var title = data.title ? data.title : data.length && data.length === 1 && data[0].title ? data[0].title : '';
    var dueDate = data.dueDate ? data.dueDate.split(" ")[0] : '';
    message.options.url = url;
    message.options.task = decodeURI(title);
    message.options.dueDate = dueDate;
  },
  initSuggester: function($, chatServices) {
    var $taskAssigneeSuggestor = $('#taskAssignee');
    if (!$taskAssigneeSuggestor.length) {
      return;
    }
    if(!$taskAssigneeSuggestor[0].selectize) {
      //init suggester
      $taskAssigneeSuggestor.suggester({
        type : 'tag',
        plugins: ['remove_button'],
        valueField: 'name',
        labelField: 'fullname',
        searchField: ['fullname'],
        sourceProviders: ['exo:task-add-user'],
        providers: {
          'exo:task-add-user': function(query, callback) {
            if (!query || !query.trim().length) {
              return callback();
            }
            chatServices.getChatUsers(eXo.chat.userSettings, query.trim()).then(function(data) {
              if(data && data.users) {
                callback(data.users);
              }
            });
          }
        },
        renderMenuItem: function(item, escape) {
          var avatar = chatServices.getUserAvatar(item.name);
          var defaultAvatar = '/chat/img/room-default.jpg';
          return '<div class="avatarMini"> \
              <img src="' + avatar + '" onerror="this.src=\'' + defaultAvatar + '\'"> \
            </div> \
            <div class="user-name">' + escape(item.fullname)+ ' (' + item.name + ')</div> \
            <div class="user-status"><i class="chat-status-' + item.status + '"></i></div>';
        }
      });
    } else {
      //clear suggester
      $taskAssigneeSuggestor.suggester('setValue', '');
      $taskAssigneeSuggestor[0].selectize.clear(true);
      $taskAssigneeSuggestor[0].selectize.renderCache['item'] = {};
    }
  },
  saveTask: function(userSettings, data) {
    return fetch('/chat/api/1.0/plugin/action', {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
      },
      credentials: 'include',
      method: 'post',
      body: decodeURI(this.$.param(data))
    });
  }
};
