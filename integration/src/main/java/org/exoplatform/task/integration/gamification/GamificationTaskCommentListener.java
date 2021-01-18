package org.exoplatform.task.integration.gamification;

import org.exoplatform.addons.gamification.entities.domain.effective.GamificationActionsHistory;
import org.exoplatform.addons.gamification.service.configuration.RuleService;
import org.exoplatform.addons.gamification.service.effective.GamificationService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.task.dto.CommentDto;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.util.TaskUtil;

public class GamificationTaskCommentListener extends Listener<TaskService, CommentDto> {
  private static final Log      LOG                                  =
                                    ExoLogger.getLogger(GamificationTaskCommentListener.class);

  private static final String   GAMIFICATION_TASK_ADDON_COMMENT_TASK = "commentTask";

  protected RuleService         ruleService;

  protected IdentityManager     identityManager;

  protected GamificationService gamificationService;

  public GamificationTaskCommentListener(RuleService ruleService,
                                         IdentityManager identityManager,
                                         GamificationService gamificationService) {
    this.ruleService = ruleService;
    this.identityManager = identityManager;
    this.gamificationService = gamificationService;
  }

  @Override
  public void onEvent(Event<TaskService, CommentDto> event) throws Exception {
    String actorUsername = ConversationState.getCurrent().getIdentity().getUserId();

    GamificationActionsHistory aHistory = null;

    // Compute user id
    String actorId = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, actorUsername, false).getId();

    gamificationService.createHistory(GAMIFICATION_TASK_ADDON_COMMENT_TASK,
                                      actorId,
                                      actorId,
                                      TaskUtil.buildTaskURL(event.getData().getTask()));

  }
}
