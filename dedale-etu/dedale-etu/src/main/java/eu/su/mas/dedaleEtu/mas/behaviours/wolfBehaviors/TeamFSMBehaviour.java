package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors;

import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors.BlockingGolemBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors.ComputeAndAssignTaskBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors.ExecuteMoveBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors.HandleConnectionRequestBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors.HandleConnectionResponseBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors.ObserveAfterMoveBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors.RequestConnectionBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors.VerifyGolemPosBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors.WaitMissionBehaviour;
import jade.core.behaviours.FSMBehaviour;

public class TeamFSMBehaviour extends FSMBehaviour {

    private static final long serialVersionUID = -5364606336107927150L;

    public TeamFSMBehaviour(WolfAgent agent) {
        super(agent);

        String OBSERVE_AFTER_MOVE_BEHAVIOUR = "ObserveAfterMoveBehaviour";
        registerFirstState(new ObserveAfterMoveBehaviour(agent), OBSERVE_AFTER_MOVE_BEHAVIOUR);
        
        // 收集连接响应
        String HANDLE_CONNECTION_REQUEST_BEHAVIOUR = "HandleConnectionRequestBehaviour";
        registerState(new HandleConnectionRequestBehaviour(agent), HANDLE_CONNECTION_REQUEST_BEHAVIOUR);
        
        String HANDLE_CONNECTION_RESPONSE_BEHAVIOUR = "HandleConnectionResponseBehaviour";
        registerState(new HandleConnectionResponseBehaviour(agent), HANDLE_CONNECTION_RESPONSE_BEHAVIOUR);
        
        // 请求连接
        String REQUEST_CONNECTION_BEHAVIOUR = "RequestConnectionBehaviour";
        registerState(new RequestConnectionBehaviour(agent), REQUEST_CONNECTION_BEHAVIOUR);

        // 计算并分配任务（仅限Chef）
        String COMPUTE_AND_ASSIGN_TASK_BEHAVIOUR = "ComputeAndAssignTaskBehaviour";
        registerState(new ComputeAndAssignTaskBehaviour(agent), COMPUTE_AND_ASSIGN_TASK_BEHAVIOUR);

        String WAIT_MISSION_BEHAVIOUR = "WaitMissionBehaviour";
        registerState(new WaitMissionBehaviour(agent,1000), WAIT_MISSION_BEHAVIOUR);
        
        // 执行移动
        String EXECUTE_MOVE_BEHAVIOUR = "ExecuteMoveBehaviour";
        registerState(new ExecuteMoveBehaviour(agent), EXECUTE_MOVE_BEHAVIOUR);

        String VERIFY_GOLEM_POS_BEHAVIOUR = "VerifyGolemPosBehaviour";
        registerState(new VerifyGolemPosBehaviour(agent), VERIFY_GOLEM_POS_BEHAVIOUR);

        String BLOCKING_GOLEM_BEHAVIOUR = "BlockingGolemBehaviour";
        registerState(new BlockingGolemBehaviour(agent), BLOCKING_GOLEM_BEHAVIOUR);

        // 注册状态
        registerDefaultTransition(OBSERVE_AFTER_MOVE_BEHAVIOUR, HANDLE_CONNECTION_REQUEST_BEHAVIOUR);        
        
        registerTransition(HANDLE_CONNECTION_REQUEST_BEHAVIOUR, WAIT_MISSION_BEHAVIOUR, 1); // 1: 收到至少一个响应
        registerDefaultTransition(WAIT_MISSION_BEHAVIOUR, EXECUTE_MOVE_BEHAVIOUR);
        
        registerTransition(HANDLE_CONNECTION_REQUEST_BEHAVIOUR, REQUEST_CONNECTION_BEHAVIOUR,0);
        registerDefaultTransition(REQUEST_CONNECTION_BEHAVIOUR, HANDLE_CONNECTION_RESPONSE_BEHAVIOUR);
        registerDefaultTransition(HANDLE_CONNECTION_RESPONSE_BEHAVIOUR, COMPUTE_AND_ASSIGN_TASK_BEHAVIOUR); // 1: 收到至少一个响应
        registerTransition(COMPUTE_AND_ASSIGN_TASK_BEHAVIOUR, EXECUTE_MOVE_BEHAVIOUR,0); // 0: 未收到响应
        registerTransition(COMPUTE_AND_ASSIGN_TASK_BEHAVIOUR, VERIFY_GOLEM_POS_BEHAVIOUR,1);
        registerDefaultTransition(VERIFY_GOLEM_POS_BEHAVIOUR, COMPUTE_AND_ASSIGN_TASK_BEHAVIOUR);

        registerTransition(EXECUTE_MOVE_BEHAVIOUR, OBSERVE_AFTER_MOVE_BEHAVIOUR,0);

        registerTransition(EXECUTE_MOVE_BEHAVIOUR, BLOCKING_GOLEM_BEHAVIOUR,1);
        
    
        // TEST AUTONOMOUS BEHAVIOUR
        // registerDefaultTransition(OBSERVE_AFTER_MOVE_BEHAVIOUR, COMPUTE_AND_ASSIGN_TASK_BEHAVIOUR);
        // registerDefaultTransition(COMPUTE_AND_ASSIGN_TASK_BEHAVIOUR, EXECUTE_MOVE_BEHAVIOUR);
        // registerDefaultTransition(EXECUTE_MOVE_BEHAVIOUR, OBSERVE_AFTER_MOVE_BEHAVIOUR);

    }
}
