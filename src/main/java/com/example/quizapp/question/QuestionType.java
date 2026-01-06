package com.example.quizapp.question;

/**
 * Enum representing different types of quiz questions
 *
 * Supports 8 question types as per requirements:
 * 1. Single choice - wybór jednej opcji
 * 2. Multiple choice - wybór wielu opcji
 * 3. True/False - prawda/fałsz
 * 4. Short answer - krótka odpowiedź tekstowa
 * 5. Dropdown - lista wyboru
 * 6. Fill blanks - uzupełnianie luk
 * 7. Sorting - sortowanie elementów
 * 8. Matching - dopasowywanie par
 */
public enum QuestionType {

    /**
     * Single choice question - user selects one correct answer from multiple options
     * Example: "What is the capital of Poland?" A) Warsaw B) Krakow C) Gdansk
     */
    SINGLE_CHOICE("Single Choice", "Select one correct answer"),

    /**
     * Multiple choice question - user can select multiple correct answers
     * Example: "Which are programming languages?" A) Java B) HTML C) Python D) CSS
     */
    MULTIPLE_CHOICE("Multiple Choice", "Select all correct answers"),

    /**
     * True/False question - user selects whether statement is true or false
     * Example: "Java is a programming language. True or False?"
     */
    TRUE_FALSE("True/False", "Select True or False"),

    /**
     * Short answer question - user types a short text answer
     * Example: "What is the capital of France?"
     */
    SHORT_ANSWER("Short Answer", "Type your answer"),

    /**
     * Dropdown list question - user selects one answer from dropdown
     * Example: "Select your country: [dropdown with countries]"
     */
    DROPDOWN("Dropdown List", "Select from dropdown"),

    /**
     * Fill in the blanks - user fills missing words in text
     * Example: "Java was created by _____ in _____"
     */
    FILL_BLANKS("Fill in the Blanks", "Fill in the missing words"),

    /**
     * Sorting question - user arranges items in correct order
     * Example: "Sort these events chronologically"
     */
    SORTING("Sorting", "Arrange items in correct order"),

    /**
     * Matching question - user matches pairs of items
     * Example: "Match countries with their capitals"
     */
    MATCHING("Matching", "Match pairs correctly");

    private final String displayName;
    private final String instruction;

    QuestionType(String displayName, String instruction) {
        this.displayName = displayName;
        this.instruction = instruction;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getInstruction() {
        return instruction;
    }

    /**
     * Check if question type allows multiple answers
     */
    public boolean allowsMultipleAnswers() {
        return this == MULTIPLE_CHOICE;
    }

    /**
     * Check if question type requires text input
     */
    public boolean requiresTextInput() {
        return this == SHORT_ANSWER || this == FILL_BLANKS;
    }

    /**
     * Check if question type requires ordering
     */
    public boolean requiresOrdering() {
        return this == SORTING;
    }

    /**
     * Check if question type requires matching pairs
     */
    public boolean requiresMatching() {
        return this == MATCHING;
    }
}