package com.pvz.game;

import com.pvz.model.enums.GameStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

public class CommandLineGame {

    private final GameSession session;
    private final GameCommandController controller;

    public CommandLineGame(GameSession session) {
        this.session = session;
        this.controller = new GameCommandController(session);
    }

    public void run(Reader input, Writer output) throws IOException {
        BufferedReader reader = new BufferedReader(input);
        PrintWriter writer = new PrintWriter(output, true);
        session.start();
        writer.print(controller.showMap());
        writer.flush();
        String command;
        while (session.status == GameStatus.RUNNING
                && (command = reader.readLine()) != null) {
            String result = controller.execute(command);
            if (result != null && !result.isEmpty()) {
                writer.println(result);
            }
        }
    }
}
