<%
    String itemsLimit = System.getProperty("exo.dw.page.snapshot.itemsLimit", "10");
%>

<div class="VuetifyApp">
    <div id="tasks">
        <script>
            require(['SHARED/tasksBundle'], function(tasksApp) {
                tasksApp.init('<%=itemsLimit%>');
            });
        </script>
    </div>
</div>