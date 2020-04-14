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
        <div data-app="true"
             class="v-application v-application--is-ltr theme--light"
             id="tasks">
            <div class="v-application--wrap">
                <div class="container pa-0">
                    <div aria-busy="true" aria-live="polite" role="alert" class="v-skeleton-loader v-skeleton-loader--is-loading theme--light">
                        <div class="v-skeleton-loader__card-heading v-skeleton-loader__bone">
                            <div class="v-skeleton-loader__heading v-skeleton-loader__bone"></div>
                        </div>
                    </div>
                    <div class="flex xs12">
                        <div class="layout">
                            <div class="flex xs6">
                                <div aria-busy="true" aria-live="polite" role="alert" class="v-skeleton-loader ml-4 v-skeleton-loader--is-loading theme--light">
                                    <div class="v-skeleton-loader__button v-skeleton-loader__bone"></div>
                                </div>
                            </div>
                            <div class="flex xs6">
                                <div aria-busy="true" aria-live="polite" role="alert" class="v-skeleton-loader ml-4 v-skeleton-loader--is-loading theme--light">
                                    <div class="v-skeleton-loader__button v-skeleton-loader__bone"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="flex xs12">
                        <div class="layout">
                            <div class="flex taskList xs7">
                                <div aria-busy="true" aria-live="polite" role="alert" class="v-skeleton-loader ml-4 mt-5 v-skeleton-loader--is-loading theme--light" style="width: 100%;">
                                    <div class="v-skeleton-loader__heading v-skeleton-loader__bone"></div>
                                </div>
                                <div aria-busy="true" aria-live="polite" role="alert" class="v-skeleton-loader ml-4 v-skeleton-loader--is-loading theme--light" style="width: 105%;">
                                    <div class="v-skeleton-loader__heading v-skeleton-loader__bone"></div>
                                </div>
                                <div aria-busy="true" aria-live="polite" role="alert" class="v-skeleton-loader ml-4 v-skeleton-loader--is-loading theme--light" style="width: 70%;">
                                    <div class="v-skeleton-loader__heading v-skeleton-loader__bone"></div>
                                </div>
                                <div aria-busy="true" aria-live="polite" role="alert" class="v-skeleton-loader ml-4 v-skeleton-loader--is-loading theme--light" style="width: 102%;">
                                    <div class="v-skeleton-loader__heading v-skeleton-loader__bone"></div>
                                </div>
                                <div aria-busy="true" aria-live="polite" role="alert" class="v-skeleton-loader ml-4 v-skeleton-loader--is-loading theme--light" style="width: 105%;">
                                    <div class="v-skeleton-loader__heading v-skeleton-loader__bone"></div>
                                </div>
                                <div aria-busy="true" aria-live="polite" role="alert" class="v-skeleton-loader ml-4 v-skeleton-loader--is-loading theme--light" style="width: 70%;">
                                    <div class="v-skeleton-loader__heading v-skeleton-loader__bone"></div>
                                </div>
                                <div aria-busy="true" aria-live="polite" role="alert" class="v-skeleton-loader ml-4 v-skeleton-loader--is-loading theme--light" style="width: 105%;">
                                    <div class="v-skeleton-loader__heading v-skeleton-loader__bone"></div>
                                </div>
                            </div>
                            <div class="flex projectList xs5">
                                <div aria-busy="true" aria-live="polite" role="alert" class="v-skeleton-loader ml-4 mt-5 v-skeleton-loader--is-loading theme--light">
                                    <div class="v-skeleton-loader__heading v-skeleton-loader__bone"></div>
                                    <div class="v-skeleton-loader__heading v-skeleton-loader__bone"></div>
                                    <div class="v-skeleton-loader__heading v-skeleton-loader__bone"></div>
                                    <div class="v-skeleton-loader__heading v-skeleton-loader__bone"></div>
                                    <div class="v-skeleton-loader__heading v-skeleton-loader__bone"></div>
                                    <div class="v-skeleton-loader__heading v-skeleton-loader__bone"></div>
                                    <div class="v-skeleton-loader__heading v-skeleton-loader__bone"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>