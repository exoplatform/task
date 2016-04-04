(function($) {
    var locale = {
        'in' : '${label.in}',
        'any': '${label.any}',
        inactive: '${label.inactive}',
        dueDate: '${label.dueDate}',
        coworkers: '${label.coworkers}',
        coworker: '${label.coworker}',
        unassigned: '${label.unassigned}',
        label: '${label.label}',
        labels: '${label.labels}',
        createLabel: '${label.createLabel}',
        noMatch: '${label.noMatch}',
        remove: '${label.remove}',
        selectUser: '${popup.selectUser}',
        noManager: '${message.noManager}',
        noParticipator: '${message.noParticipator}',
        taskDescriptionEmpty: '${editinline.taskDescription.empty}',
        noPermissionToAccessProject: '${popup.msg.noPermissionToAccessProject}',
        markAsCompleted: '${message.markAsCompleted}',
        markAsUnCompleted: '${message.markAsUnCompleted}',
        projectDescriptionEmpty: '${editinline.projectDescription.empty}',
        taskPlan: {
            errorMessage: '${editinline.taskPlan.errorMessage}'
        }
    };

    locale.resolve = function(key, params) {
        var message = key;
        if (locale[key] != undefined) {
            message = locale[key];
        }
        if (params != undefined) {
            if (!$.isArray(params)) {
                params = [params];
            }

            for (var i = 0; i < params.length; i++) {
                var reg = new RegExp('\\{' + i + '\\}', 'g');
                message = message.replace(reg, params);
            }
        }
        return message;
    };

    return locale;
})($);