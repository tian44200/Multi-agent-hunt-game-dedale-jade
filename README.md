# Hunt The Wumpus - Multi-Agent System

This project was completed during my first year of the Master’s program as part of the FoSyMa (Fundamentals of Multi-Agent Systems) course at Sorbonne Université in France, and it was presented in April 2024. However, this is still a draft version. I will release a refined version later..

## Project Overview

This project is a multi-agent system implementation of the classic game "Hunt the Wumpus" (Gregory Yob, 1972). In this extended version, developed using the **JADE** platform and the **Dedale** environment, cooperative agents must explore an unknown environment and capture Golems. Agents work together to build a complete map of the environment while avoiding obstacles and strategically cornering Golems.

The project was divided into two phases:
1. **Exploration Phase**: Agents explore the map, share partial maps, and aim to build a complete map collaboratively.
2. **Hunting Phase**: After exploration, agents work together to hunt and block Golems.

## Installation

### Prerequisites
- Java Development Kit (JDK) installed
- **Eclipse IDE** installed (The project is configured to run specifically in Eclipse)

For setting up Maven and Dedale, follow the instructions provided here:  
[Dedale Installation Guide](https://dedale.gitlab.io/page/tutorial/install/)

### Project Setup

1. Clone this repository:

    ```bash
    git clone <repo_url>
    ```

2. Import the project into Eclipse:
    - Open Eclipse.
    - Go to `File > Import > Maven > Existing Maven Projects` (if using Maven).
    - Browse to the folder where you cloned the repository, and follow the prompts to finish the import process.

3. Install Maven dependencies (if applicable):
    - Right-click the project in Eclipse.
    - Select `Run As > Maven Install` to download dependencies and build the project.

4. Run the project in Eclipse:
    - Navigate to the `src/` directory and select the main class (e.g., `Principal.java` in the `princ` package).
    - Right-click on the file and select `Run As > Java Application`.
    - The JADE platform will initialize, and the agents will start running.

5. For more advanced configurations, follow the guide on deploying multiple agents:
    [Dedale Agent Deployment Guide](https://dedale.gitlab.io/page/tutorial/deployagents/).

## Project Structure

- `src/`: Contains the main source code.
  - `agents/`: Includes the agent classes and behaviors.
    - **WolfAgent** is located at `dedale-etu/dedale-etu/src/main/java/eu/su/mas/dedaleEtu/mas/agents/dummies/WolfAgent.java`.
    - **Behaviors for WolfAgent** are located at `dedale-etu/dedale-etu/src/main/java/eu/su/mas/dedaleEtu/mas/behaviours/wolfBehaviors`.
  - `utils/`: Contains utility classes such as:
    - **MapRepresentation**: A class responsible for representing the environment as a graph, allowing agents to explore and store the map structure.
    - **MapManager**: A class that manages the agents' map data, facilitates merging maps from different agents, and supports communication of map data between agents.
- `resources/`: Configuration files and assets.
- `docs/`: Project documentation including the final report.
- `README.md`: This file.

## Agent Architecture

### Wolf Agent and Behavior Management

The Wolf agent is the sole agent type used in this project, and its behavior management is divided into two main phases:

1. **Exploration Phase**: Managed by `ExploreBehaviour`, where agents share partial maps and plan routes using Dijkstra’s algorithm. 
    - **ExploreBehaviour**: This behavior manages the exploration process until the agent runs out of unexplored nodes. During this phase, the agent:
      - Uses `MapManager` to synchronize its local map with partial maps from other agents.
      - Employs two cyclic behaviors:
        - **ShareMapBehaviour**: Sends partial maps to other agents upon request.
        - **MergeMapBehaviour**: Integrates map segments received from other agents.
    - Each exploration step involves requesting maps from other agents, merging them, and calculating the next node using Dijkstra’s algorithm.
    - If an agent encounters an obstacle, it re-plans by randomly selecting a neighboring node as the next move.

2. **Hunting Phase**: Managed by `HuntFSMBehaviour`, which includes several finite state machine (FSM) behaviors for coordinating the hunting of Golems.
    - The hunting phase involves forming teams, assigning tasks, and executing moves to block and capture Golems. Key behaviors include:
      - **ObservationBehaviour**: Agents observe their surroundings and update their local map with detected Golems or obstacles.
      - **HandleConnectionRequestBehaviour**: Handles connection requests from other agents.
      - **RequestConnectionBehaviour**: Sends connection requests to form a team.
      - **ComputeAndAssignTaskBehaviour**: The team leader computes and assigns tasks to team members based on the current map.
      - **ExecuteMoveBehaviour**: Executes the assigned task, such as moving to a target node or blocking a Golem.

### Workflow Overview

Below are diagrams illustrating the transition between the exploration and hunting phases and the key behaviors involved:

<img width="695" alt="Screenshot 2024-09-15 at 16 36 22" src="https://github.com/user-attachments/assets/f7a0febc-7e5a-4e60-aba7-41a9824357de">


Figure 1: Transition from Phase 1 to Phase 2

<img width="720" alt="Screenshot 2024-09-15 at 16 36 46" src="https://github.com/user-attachments/assets/a1c2a4c3-79c1-4521-8803-a8e9f637bcde">


Figure 2: Phase 2: HuntFSMBehaviour for the hunting process

## Environment and Communication

The agents operate in an unknown, open, dynamic, and partially observable environment, designed as a non-oriented graph where nodes represent rooms, and edges represent the corridors connecting them. Agents have limited perception of their environment:
- They perceive their current room and the neighboring rooms.
- They can detect smells emitted by Golems in adjacent rooms.

Communication between agents is limited by a specific communication radius. Agents can only communicate with other agents within their communication range. This limitation necessitates strategies for efficient communication and sharing of partial maps.

## Limitations and Future Improvements

1. **Chasing Multiple Golems**: The current implementation is less efficient when multiple Golems are involved. An improvement could involve better team coordination based on Golem positions.
2. **Golem Blocking Optimization**: Agents sometimes use more resources than necessary to block a Golem. Optimizing the blocking positions could prevent resource wastage.
3. **Asynchronous Issues**: The distributed coordination sometimes leads to misinterpretation of Golem positions. Improved synchronization methods could mitigate this risk&.

## Some Results: Golems captured
![5731726410597_ pic](https://github.com/user-attachments/assets/76fa5afe-e4d8-4c92-8d69-59c11a0b81f7)
![5711726410586_ pic](https://github.com/user-attachments/assets/d8a4d9f8-b30b-4951-b111-62c88736dc8c)
![5721726410591_ pic](https://github.com/user-attachments/assets/c44b0d9c-c89d-425a-a624-84c54cdb7a0a)

## Conclusion

This project demonstrated the complexity of multi-agent cooperation and communication. Despite the challenges, the system functions effectively in the majority of cases, and there are clear avenues for future improvements.

## Documentation

For detailed information on the project, please contact thuang44@gmail.com
