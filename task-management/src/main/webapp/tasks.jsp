<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.exoplatform.services.resources.ResourceBundleService" %>
<%@ page import="org.exoplatform.container.PortalContainer" %><%
    
    String itemsLimit = System.getProperty("exo.dw.page.snapshot.itemsLimit", "10");
    
    PortalContainer portalContainer = PortalContainer.getCurrentInstance(session.getServletContext());
    ResourceBundleService resourceBundleService = portalContainer.getComponentInstanceOfType(ResourceBundleService.class);
    ResourceBundle resourceBundle = resourceBundleService.getResourceBundle("locale.portlet.taskManagement", request.getLocale());
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
                    <div class="flex d-flex xs12 px-3">
                        <div class="layout row mx-0 align-center">
                            <div class="flex d-flex xs12">
                                <div class="v-card v-card--flat v-sheet theme--light transparent">
                                    <a>
                                        <div class="v-card__text body-1 text-uppercase color-title px-0">
                                            <%=resourceBundle.getString("label.tasks.header")%>
                                        </div>
                                    </a></div>
                            </div>
                        </div>
                    </div>
                    <div class="flex xs12">
                        <div class="layout">
                            <div class="flex xs6">
                                <div aria-busy="true" aria-live="polite" role="alert" class="v-skeleton-loader ml-4 v-skeleton-loader--is-loading">
                                    <div class="v-skeleton-loader__button v-skeleton-loader__bone" style="width: 90%;"></div>
                                </div>
                            </div>
                            <div class="flex xs6 mr-4">
                                <div aria-busy="true" aria-live="polite" role="alert" class="v-skeleton-loader ml-4 v-skeleton-loader--is-loading">
                                    <div class="v-skeleton-loader__button v-skeleton-loader__bone" style="width: 90%;"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="flex xs12">
                        <div class="layout">
                            <div class="flex taskList xs7 ml-4 mt-6">
                                <div href="#" class="v-list-item__content pa-0 pb-2">
                                    <div class="v-list-item__title">
                                        <div
                                                class="v-skeleton-loader mt-3 mr-3 skeleton-background v-skeleton-loader--boilerplate v-skeleton-loader--is-loading theme--light"
                                                style="height: 11px">
                                            <div class="v-skeleton-loader__text v-skeleton-loader__bone"></div>
                                        </div>
                                    </div>
                                    <div class="v-list-item__subtitle">
                                        <div
                                                class="v-skeleton-loader mb-2 mt-1 skeleton-background v-skeleton-loader--boilerplate v-skeleton-loader--is-loading theme--light"
                                                style="height: 8px; width: 70px;">
                                            <div class="v-skeleton-loader__text v-skeleton-loader__bone"></div>
                                        </div>
                                    </div>
                                </div>
                                <div href="#" class="v-list-item__content pa-0 pb-2" >
                                    <div class="v-list-item__title">
                                        <div
                                                class="v-skeleton-loader mt-3 mr-3 skeleton-background v-skeleton-loader--boilerplate v-skeleton-loader--is-loading theme--light"
                                                style="height: 11px">
                                            <div class="v-skeleton-loader__text v-skeleton-loader__bone"></div>
                                        </div>
                                    </div>
                                    <div class="v-list-item__subtitle">
                                        <div
                                                class="v-skeleton-loader mb-2 mt-1 skeleton-background v-skeleton-loader--boilerplate v-skeleton-loader--is-loading theme--light"
                                                style="height: 8px; width: 70px;">
                                            <div class="v-skeleton-loader__text v-skeleton-loader__bone"></div>
                                        </div>
                                    </div>
                                </div>
                                <div href="#" class="v-list-item__content pa-0 pb-2">
                                    <div class="v-list-item__title">
                                        <div
                                                class="v-skeleton-loader mt-3 mr-3 skeleton-background v-skeleton-loader--boilerplate v-skeleton-loader--is-loading theme--light"
                                                style="height: 11px;">
                                            <div class="v-skeleton-loader__text v-skeleton-loader__bone"></div>
                                        </div>
                                    </div>
                                    <div class="v-list-item__subtitle">
                                        <div
                                                class="v-skeleton-loader mb-2 mt-1 skeleton-background v-skeleton-loader--boilerplate v-skeleton-loader--is-loading theme--light"
                                                style="height: 8px;width: 70px;">
                                            <div class="v-skeleton-loader__text v-skeleton-loader__bone"></div>
                                        </div>
                                    </div>
                                </div>
                                <div href="#" class="v-list-item__content pa-0 pb-2">
                                    <div class="v-list-item__title">
                                        <div
                                                class="v-skeleton-loader mt-3 mr-3 skeleton-background v-skeleton-loader--boilerplate v-skeleton-loader--is-loading theme--light"
                                                style="height: 11px">
                                            <div class="v-skeleton-loader__text v-skeleton-loader__bone"></div>
                                        </div>
                                    </div>
                                    <div class="v-list-item__subtitle">
                                        <div
                                                class="v-skeleton-loader mb-2 mt-1 skeleton-background v-skeleton-loader--boilerplate v-skeleton-loader--is-loading theme--light"
                                                style="height: 8px; width: 70px;">
                                            <div class="v-skeleton-loader__text v-skeleton-loader__bone"></div>
                                        </div>
                                    </div>
                                </div>
                                <div href="#" class="v-list-item__content pa-0 pb-2">
                                    <div class="v-list-item__title">
                                        <div
                                                class="v-skeleton-loader mt-3 mr-3 skeleton-background v-skeleton-loader--boilerplate v-skeleton-loader--is-loading theme--light"
                                                style="height: 11px;">
                                            <div class="v-skeleton-loader__text v-skeleton-loader__bone"></div>
                                        </div>
                                    </div>
                                    <div class="v-list-item__subtitle">
                                        <div
                                                class="v-skeleton-loader mb-2 mt-1 skeleton-background v-skeleton-loader--boilerplate v-skeleton-loader--is-loading theme--light"
                                                style="height: 8px; width: 70px;">
                                            <div class="v-skeleton-loader__text v-skeleton-loader__bone"></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="flex projectList xs5">
                                <div aria-busy="true" aria-live="polite" role="alert" class="v-skeleton-loader ml-4 mt-5 v-skeleton-loader--is-loading theme--light">
                                    <div class="v-list-item__title pb-4">
                                        <div
                                                class="v-skeleton-loader mt-3 mr-3 skeleton-background v-skeleton-loader--boilerplate v-skeleton-loader--is-loading theme--light"
                                                style="height: 24px;width: 80%;">
                                            <div class="v-skeleton-loader__text v-skeleton-loader__bone"></div>
                                        </div>
                                    </div>
                                    <div class="v-list-item__title pb-4">
                                        <div
                                                class="v-skeleton-loader mt-3 mr-3 skeleton-background v-skeleton-loader--boilerplate v-skeleton-loader--is-loading theme--light"
                                                style="height: 24px;width: 80%;">
                                            <div class="v-skeleton-loader__text v-skeleton-loader__bone"></div>
                                        </div>
                                    </div>
                                    <div class="v-list-item__title pb-4">
                                        <div
                                                class="v-skeleton-loader mt-3 mr-3 skeleton-background v-skeleton-loader--boilerplate v-skeleton-loader--is-loading theme--light"
                                                style="height: 24px;width: 80%;">
                                            <div class="v-skeleton-loader__text v-skeleton-loader__bone"></div>
                                        </div>
                                    </div>
                                    <div class="v-list-item__title pb-4">
                                        <div
                                                class="v-skeleton-loader mt-3 mr-3 skeleton-background v-skeleton-loader--boilerplate v-skeleton-loader--is-loading theme--light"
                                                style="height: 24px;width: 80%;">
                                            <div class="v-skeleton-loader__text v-skeleton-loader__bone"></div>
                                        </div>
                                    </div>
                                    <div class="v-list-item__title pb-4">
                                        <div
                                                class="v-skeleton-loader mt-3 mr-3 skeleton-background v-skeleton-loader--boilerplate v-skeleton-loader--is-loading theme--light"
                                                style="height: 24px;width: 80%;">
                                            <div class="v-skeleton-loader__text v-skeleton-loader__bone"></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>