package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors;

import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors.ComputeAndAssignTaskBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors.ExecuteMoveBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors.HandleConnectionRequestBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors.ObserveAfterMoveBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors.ReceiveTaskBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors.ReportObservationsBehavior;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors.RequestConnectionBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors.TeamSetUpBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors.ShareConnectionsBehaviour;
import jade.core.behaviours.FSMBehaviour;

public class TeamFSMBehaviour extends FSMBehaviour {

    private static final long serialVersionUID = -5364606336107927150L;

    public TeamFSMBehaviour(WolfAgent agent) {
        super(agent);

        String OBSERVE_AFTER_MOVE_BEHAVIOUR = "ObserveAfterMoveBehaviour";
        registerFirstState(new ObserveAfterMoveBehaviour(agent), OBSERVE_AFTER_MOVE_BEHAVIOUR);
        
        // 请求连接
        String REQUEST_CONNECTION_BEHAVIOUR = "RequestConnectionBehaviour";
        registerState(new RequestConnectionBehaviour(agent), REQUEST_CONNECTION_BEHAVIOUR);

        // 收集连接响应
        String HANDLE_CONNECTION_REQUEST_BEHAVIOUR = "HandleConnectionRequestBehaviour";
        registerState(new HandleConnectionRequestBehaviour(agent,10000), HANDLE_CONNECTION_REQUEST_BEHAVIOUR);
        

        // 分享连接信息
        String SHARE_CONNECTIONS_BEHAVIOUR = "ShareConnectionsBehaviour";
        registerState(new ShareConnectionsBehaviour(agent,1000), SHARE_CONNECTIONS_BEHAVIOUR);

        // 选举Chef
        String TEAM_SET_UP_BEHAVIOUR = "TeamSetUpBehaviour";
        registerState(new TeamSetUpBehaviour(agent), TEAM_SET_UP_BEHAVIOUR);

        // 分享观测信息
        String REPORT_OBSERVATIONS_BEHAVIOUR = "ReportObservationsBehavior";
        registerState(new ReportObservationsBehavior(agent), REPORT_OBSERVATIONS_BEHAVIOUR);

        // 计算并分配任务（仅限Chef）
        String COMPUTE_AND_ASSIGN_TASK_BEHAVIOUR = "ComputeAndAssignTaskBehaviour";
        registerState(new ComputeAndAssignTaskBehaviour(agent), COMPUTE_AND_ASSIGN_TASK_BEHAVIOUR);

        String RECEIVE_TASK_BEHAVIOUR = "ReceiveTaskBehaviour";
        registerState(new ReceiveTaskBehaviour(agent), RECEIVE_TASK_BEHAVIOUR);
        
        // 执行移动
        String EXECUTE_MOVE_BEHAVIOUR = "ExecuteMoveBehaviour";
        registerState(new ExecuteMoveBehaviour(agent), EXECUTE_MOVE_BEHAVIOUR);


        // 注册状态
        registerDefaultTransition(OBSERVE_AFTER_MOVE_BEHAVIOUR, REQUEST_CONNECTION_BEHAVIOUR);        
        registerDefaultTransition(REQUEST_CONNECTION_BEHAVIOUR, HANDLE_CONNECTION_REQUEST_BEHAVIOUR);

        registerDefaultTransition(HANDLE_CONNECTION_REQUEST_BEHAVIOUR, COMPUTE_AND_ASSIGN_TASK_BEHAVIOUR);
        registerTransition(HANDLE_CONNECTION_REQUEST_BEHAVIOUR, SHARE_CONNECTIONS_BEHAVIOUR, 1); // 1: 收到至少一个响应
    
        registerDefaultTransition(SHARE_CONNECTIONS_BEHAVIOUR, TEAM_SET_UP_BEHAVIOUR); // 0: 未收到响应
        registerDefaultTransition(TEAM_SET_UP_BEHAVIOUR, REPORT_OBSERVATIONS_BEHAVIOUR); // 0: 未收到响应
        
        registerDefaultTransition(REPORT_OBSERVATIONS_BEHAVIOUR, RECEIVE_TASK_BEHAVIOUR);
        registerTransition(REPORT_OBSERVATIONS_BEHAVIOUR, COMPUTE_AND_ASSIGN_TASK_BEHAVIOUR,1);
        
        registerDefaultTransition(COMPUTE_AND_ASSIGN_TASK_BEHAVIOUR, EXECUTE_MOVE_BEHAVIOUR);
        registerDefaultTransition(RECEIVE_TASK_BEHAVIOUR, EXECUTE_MOVE_BEHAVIOUR);

        registerDefaultTransition(EXECUTE_MOVE_BEHAVIOUR, OBSERVE_AFTER_MOVE_BEHAVIOUR);

        // TEST AUTONOMOUS BEHAVIOUR
        // registerDefaultTransition(OBSERVE_AFTER_MOVE_BEHAVIOUR, COMPUTE_AND_ASSIGN_TASK_BEHAVIOUR);
        // registerDefaultTransition(COMPUTE_AND_ASSIGN_TASK_BEHAVIOUR, EXECUTE_MOVE_BEHAVIOUR);
        // registerDefaultTransition(EXECUTE_MOVE_BEHAVIOUR, OBSERVE_AFTER_MOVE_BEHAVIOUR);

    }
}
