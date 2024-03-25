package eu.su.mas.dedaleEtu.mas.behaviours.huntBehaviors;
import eu.su.mas.dedaleEtu.mas.agents.dummies.HuntAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapManager;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;

public class HuntFSMBehaviour extends FSMBehaviour {

    private static final long serialVersionUID = -7275880621857196085L;
    private static final String STATE_IMMOBILE = "Immobile";
    private static final String STATE_WALK = "Walk";
    private static final String STATE_CHECK_RECEIVE = "CheckReceive";
    private static final String STATE_SHARE_INFO = "ShareInfo";
    private static final String STATE_REQUEST_INFO = "RequestInfo";

    public HuntFSMBehaviour(Agent a) {
        super(a);

        DataStore ds = new DataStore();

        // 创建状态实例
        CheckReceiveBehaviour checkReceive = new CheckReceiveBehaviour(a);
        ShareInfoBehaviour shareInfo = new ShareInfoBehaviour((HuntAgent)a);

        // 分配相同的DataStore给所有状态
        checkReceive.setDataStore(ds);
        shareInfo.setDataStore(ds);

        // Register state behaviours
        registerFirstState(new WalkBehaviour((HuntAgent)a), STATE_WALK);
        registerState(new ImmobileBehaviour((HuntAgent)a), STATE_IMMOBILE);
        registerState(checkReceive, STATE_CHECK_RECEIVE);
        registerState(shareInfo, STATE_SHARE_INFO);
        registerState(new RequestInfoBehaviour(a), STATE_REQUEST_INFO);



        // Register transitions
        registerTransition(STATE_CHECK_RECEIVE, STATE_CHECK_RECEIVE,0);
        registerTransition(STATE_CHECK_RECEIVE, STATE_WALK, 3);
        registerTransition(STATE_CHECK_RECEIVE, STATE_SHARE_INFO, 1);
        registerTransition(STATE_CHECK_RECEIVE, STATE_REQUEST_INFO, 2);
        // registerTransition(STATE_WALK, STATE_IMMOBILE, 1);
        registerTransition(STATE_WALK, STATE_CHECK_RECEIVE,1);
        registerDefaultTransition(STATE_IMMOBILE, STATE_CHECK_RECEIVE);
        registerDefaultTransition(STATE_SHARE_INFO, STATE_CHECK_RECEIVE);
        registerDefaultTransition(STATE_REQUEST_INFO, STATE_WALK);
    }
}

