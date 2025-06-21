package net.coosanta.meldmc.gui.nodes.text;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for parsing and displaying formatted text in JavaFX TextFlow components.
 * Handles Minecraft style formatting codes and converts them to appropriate JavaFX text styles.
 */
public class FormattedTextParser {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Mapping of Minecraft formatting codes to CSS class names
     */
    private static final Map<Character, String> FORMATTING_CODES = Map.ofEntries(
            Map.entry('0', "mc-black"),
            Map.entry('1', "mc-dark-blue"),
            Map.entry('2', "mc-dark-green"),
            Map.entry('3', "mc-dark-aqua"),
            Map.entry('4', "mc-dark-red"),
            Map.entry('5', "mc-dark-purple"),
            Map.entry('6', "mc-gold"),
            Map.entry('7', "mc-gray"),
            Map.entry('8', "mc-dark-gray"),
            Map.entry('9', "mc-blue"),
            Map.entry('a', "mc-green"),
            Map.entry('b', "mc-aqua"),
            Map.entry('c', "mc-red"),
            Map.entry('d', "mc-light-purple"),
            Map.entry('e', "mc-yellow"),
            Map.entry('f', "mc-white"),
            Map.entry('l', "mc-bold"),
            Map.entry('m', "mc-strikethrough"),
            Map.entry('n', "mc-underline"),
            Map.entry('o', "mc-italic")
    );

    /**
     * Updates the provided TextFlow with formatted text from an Adventure Component.
     *
     * @param textFlow       The TextFlow to update
     * @param component      The Adventure Component containing the text
     * @param baseStyleClass The base style class to apply to all text nodes
     */
    public static void updateTextFlow(TextFlow textFlow, Component component, String baseStyleClass) {
        textFlow.getStyleClass().clear();
        textFlow.getChildren().clear();

        if (component == null) {
            return;
        }

        String formattedText = miniMessage.serialize(component);
        parseFormattedText(textFlow, formattedText, baseStyleClass);
    }

    /**
     * Updates the TextFlow with status text when no component is available.
     * <p>
     * Possibly will be refactored -- "irrelevant" method to the class.
     *
     * @param textFlow         The TextFlow to update
     * @param status           The status text to display
     * @param statusStyleClass The style class to apply to the status text
     */
    public static void updateTextFlowWithStatus(TextFlow textFlow, String status, String statusStyleClass) {
        textFlow.getStyleClass().clear();
        textFlow.getChildren().clear();

        Text text = new Text(status);
        text.getStyleClass().add(statusStyleClass);
        textFlow.getChildren().add(text);
    }

    /**
     * Parses formatted text with Minecraft formatting codes and adds the resulting
     * Text nodes to the provided TextFlow.
     *
     * @param textFlow       The TextFlow to add the parsed text to
     * @param text           The formatted text to parse
     * @param baseStyleClass The base style class to apply to all text nodes
     */
    public static void parseFormattedText(TextFlow textFlow, String text, String baseStyleClass) {
        if (textFlow == null || text == null) {
            return;
        }

        StringBuilder currentText = new StringBuilder();
        Map<String, Boolean> activeClasses = new HashMap<>();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '§' && i + 1 < text.length()) {
                addTextNode(textFlow, currentText, activeClasses, baseStyleClass);

                char formatCode = text.charAt(++i);
                if (formatCode == 'r') {
                    activeClasses.clear();
                } else if (FORMATTING_CODES.containsKey(formatCode)) {
                    String cssClass = FORMATTING_CODES.get(formatCode);
                    activeClasses.put(cssClass, true);
                } // TODO: explore default minecraft behaviour with invalid formatting codes
            } else {
                currentText.append(c);
            }
        }

        addTextNode(textFlow, currentText, activeClasses, baseStyleClass);
    }

    /**
     * Adds a text node with the provided content and styles to the TextFlow.
     *
     * @param textFlow       The TextFlow to add the text node to
     * @param currentText    The StringBuilder containing the text content
     * @param activeClasses  The map of active CSS classes to apply
     * @param baseStyleClass The base style class to apply
     */
    private static void addTextNode(TextFlow textFlow, StringBuilder currentText,
                                    Map<String, Boolean> activeClasses, String baseStyleClass) {
        if (currentText.isEmpty()) return;

        Text textNode = new Text(currentText.toString());
        textNode.getStyleClass().add(baseStyleClass);

        for (Map.Entry<String, Boolean> entry : activeClasses.entrySet()) {
            if (entry.getValue()) {
                textNode.getStyleClass().add(entry.getKey());
            }
        }

        textFlow.getChildren().add(textNode);
        currentText.setLength(0);
    }
}
