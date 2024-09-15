# Hunt The Wumpus - Multi-Agent System

This project was completed during my first year of the Master’s program as part of the FoSyMa (Fundamentals of Multi-Agent Systems) course at Sorbonne Université, and was presented in April 2024.

## Project Overview

This project is a multi-agent system implementation of the classic game "Hunt the Wumpus" (Gregory Yob, 1972). In this extended version, developed using the **JADE** platform and the **Dedale** environment, cooperative agents must explore an unknown environment, collect treasures, and capture Golems. Agents work together to build a complete map of the environment while avoiding obstacles and strategically cornering Golems.

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

### Wolf Agent
The project uses a single type of agent, **Wolf**, which functions cooperatively with other agents. It is implemented in the file `WolfAgent.java` located at `dedale-etu/dedale-etu/src/main/java/eu/su/mas/dedaleEtu/mas/agents/dummies/WolfAgent.java`. The Wolf agent consists of several behaviors, all of which are implemented in the folder `wolfBehaviors` located at `dedale-etu/dedale-etu/src/main/java/eu/su/mas/dedaleEtu/mas/behaviours/wolfBehaviors`.

The key behaviors include:
- **ExploreBehavior**: Manages the exploration phase, where agents share partial maps and navigate the environment using Dijkstra's algorithm, supported by the `MapRepresentation` class.
- **HuntFSMBehavior**: Manages the hunting phase, where agents cooperate to capture the Golems.

### Key Behaviors
1. **ShareMapBehavior**: Shares the agent's local map with others, utilizing the `MapManager` class.
2. **MergeMapBehavior**: Merges received maps from other agents using the `MapManager`.
3. **ComputeAndAssignTaskBehavior**: Coordinates the hunting tasks among agents.
4. **ExecuteMoveBehavior**: Moves the agent based on assigned tasks.
5. **BlockingGolemBehavior**: Blocks a Golem by positioning agents strategically.

## Environment and Communication

The agents operate in an unknown, open, dynamic, and partially observable environment, designed as a non-oriented graph where nodes represent rooms, and edges represent the corridors connecting them. Agents have limited perception of their environment:
- They perceive their current room and the neighboring rooms.
- They can detect smells emitted by Golems in adjacent rooms.

Communication between agents is limited by a specific communication radius. Agents can only communicate with other agents within their communication range. This limitation necessitates strategies for efficient communication and sharing of partial maps.

## Limitations and Future Improvements

1. **Chasing Multiple Golems**: The current implementation is less efficient when multiple Golems are involved. An improvement could involve better team coordination based on Golem positions.
2. **Golem Blocking Optimization**: Agents sometimes use more resources than necessary to block a Golem. Optimizing the blocking positions could prevent resource wastage.
3. **Asynchronous Issues**: The distributed coordination sometimes leads to misinterpretation of Golem positions. Improved synchronization methods could mitigate this risk&.

## Conclusion

This project demonstrated the complexity of multi-agent cooperation and communication. Despite the challenges, the system functions effectively in the majority of cases, and there are clear avenues for future improvements.

## Documentation

For detailed information on the project, please contact thuang44@gmail.com
