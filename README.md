# Scotland Yard Board Game

Welcome to the Scotland Yard Board Game project! This repository contains the source code for an interactive digital version of the classic board game "Scotland Yard." Follow the instructions below to set up your development environment and run the game.

- **Game Main Screen:**
  ![Game Main Screen](./doc/screenshots/Screenshot%20from%202024-08-25%2013-33-50.png)


## About the Game

**Scotland Yard** is a classic detective board game where players assume the roles of detectives trying to capture Mr. X, a fugitive who moves secretly across the board. The game involves strategy, deduction, and a bit of luck. This digital version aims to capture the essence of the classic game with enhanced graphics and a modern user interface.

- **Game In Progress:**
  ![Game Map In The Game Progress](./doc/screenshots/Screenshot%20from%202024-08-25%2013-34-01.png)


## Game Rules and Information

To understand the game's rules and historical context, refer to the following resources:

- **Official Rulebook:** [Scotland Yard Official Rulebook](https://init-games.blogspot.com/2020/02/scotland-yard-1983.html)
- **Wikipedia Overview:** [Scotland Yard (board game) on Wikipedia](https://en.wikipedia.org/wiki/Scotland_Yard_(board_game))

These links provide comprehensive information about the game's rules, history, and variations.

## Prerequisites

To run and develop this project, you need the following software:

### IntelliJ IDEA

1. **Download and Install IntelliJ IDEA:**
   - Visit the [IntelliJ IDEA installation guide](https://www.jetbrains.com/help/idea/installation-guide.html) to download and install the IDE.

### JavaFX

1. **Download JavaFX SDK:**
   - Obtain the JavaFX SDK from the [Gluon website](https://gluonhq.com/products/javafx/).

### Java Development Kit (JDK)

1. **Download and Install Java JDK:**
   - Download the latest version of Java from the [official website](https://www.java.com/en/).
   - **Important:** Ensure that the JDK version is correctly set in IntelliJ IDEA. Navigate to `File > Project Structure > Project Settings > Project > SDK` and configure the correct version.

## Setting Up the Project

1. **Download the Project Files:**
   - Obtain the project file from the source repository or distribution.

2. **Extract the Files:**
   - Extract the downloaded archive to your preferred directory.

3. **Open the Project in IntelliJ IDEA:**
   - Launch IntelliJ IDEA.
   - Select `File > Open` and navigate to the directory where you extracted the files.

4. **Configure JavaFX Libraries:**
   - Add the JavaFX libraries to your project:
     - Go to `File > Project Structure > Libraries`.
     - Click on `+` and select `Java`.
     - Add the JavaFX SDK libraries located in the extracted JavaFX directory (`lib` folder).

5. **Compile and Run the Project:**
   - Locate `Main.java` in the following directory:
     ```
     src/main/java/uk/ac/bris/cs/scotlandyard
     ```
   - Open `Main.java` in IntelliJ IDEA.
   - Click the `Run` button (green play button) to execute the program.

## Playing the Game

Once the program is running, you will be able to interact with the digital version of Scotland Yard. Follow the on-screen instructions to start a new game and enjoy!

- **End Game Screen:**
  ![The Game Screen](./doc/screenshots/Screenshot%20from%202024-08-25%2013-34-18.png)

  
## Troubleshooting

If you encounter any issues or errors:

- Ensure all dependencies are correctly configured.
- Verify that JavaFX libraries are properly added to your project.
- Check that the correct version of Java is set in IntelliJ IDEA.

For further assistance, consult the documentation or reach out to the project maintainers.

## Contributing

Contributions to the project are welcome. Please follow the standard GitHub workflow:

1. Fork the repository.
2. Create a new branch for your changes.
3. Submit a pull request with a description of your modifications.

---

## Important Notice

This project was developed as part of a university assignment at the University of Bristol. The code and resources provided here are the property of the University of Bristol. 

**Distribution and Use:**
- Do not distribute this project or its contents without explicit permission from the University of Bristol.
- The project is intended for educational purposes and should not be used for commercial or other unauthorised activities.

## Understanding the Project

For those interested in diving deeper into the project, here are some useful resources and documentation:

- **Project API Documentation:**
  - Review the detailed API documentation for the Scotland Yard project [here](https://seis.bristol.ac.uk/~sh1670/SY/apidocs2022/index.html). This documentation provides an overview of the functions and classes used in the project.

- **Guava Library Documentation:**
  - **Graphs Explained:** Learn about the `Graph` and `ValueGraph` data structures from the Guava library [here](https://github.com/google/guava/wiki/GraphsExplained#valuegraph).
  - **Immutable Collections Explained:** Explore the immutable collections provided by Guava [here](https://github.com/google/guava/wiki/ImmutableCollectionsExplained).

These resources will help you understand the codebase, the functionality of various components, and how to use advanced data structures effectively.

---

Thank you for your interest in Scotland Yard! Enjoy your game and happy coding!
