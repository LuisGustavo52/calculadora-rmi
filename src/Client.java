import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

public class Client extends Application {

    private Operations calc;
    private Label expressionLabel;
    private TextField display;
    private Label statusLabel;
    private TextArea historyArea;
    private String currentInput = "";
    private String operator = "";
    private double firstOperand = 0;
    private boolean newInput = true;

    private enum Mode { BASIC, SCIENTIFIC, CONVERSION }
    private Mode currentMode = Mode.BASIC;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Calculadora RMI");
        stage.setResizable(false);

        connectToServer();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1e1e2e;");

        VBox topArea = buildTopArea();
        root.setTop(topArea);

        TabPane tabPane = buildTabPane();
        root.setCenter(tabPane);

        VBox historyPanel = buildHistoryPanel();
        root.setRight(historyPanel);

        Scene scene = new Scene(root, 720, 550);
        stage.setScene(scene);
        stage.show();
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                Operations lookupServer = (Operations) Naming.lookup("rmi://localhost/CalculatorService");
                Platform.runLater(() -> {
                    calc = lookupServer;
                    updateStatus("Conectado ao servidor RMI.", true);
                    refreshHistory();
                });
            } catch (ConnectException e) {
                Platform.runLater(() -> updateStatus("Servidor não iniciado.", false));
            } catch (NotBoundException e) {
                Platform.runLater(() -> updateStatus("Serviço não encontrado.", false));
            } catch (MalformedURLException e) {
                Platform.runLater(() -> updateStatus("URL inválida.", false));
            } catch (RemoteException e) {
                Platform.runLater(() -> updateStatus("Erro de conexão remota.", false));
            } catch (Exception e) {
                Platform.runLater(() -> updateStatus("Erro desconhecido: " + e.getMessage(), false));
            }
        }).start();
    }

    private VBox buildTopArea() {
        VBox box = new VBox(4);
        box.setPadding(new Insets(16, 16, 8, 16));

        VBox displayBox = new VBox(2);
        displayBox.setAlignment(Pos.CENTER_RIGHT);
        displayBox.setStyle(
                "-fx-background-color: #2a2a3e; " +
                "-fx-border-color: #45475a; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 8 12;"
        );

        expressionLabel = new Label("");
        expressionLabel.setFont(Font.font("Consolas", 14));
        expressionLabel.setTextFill(Color.web("#a6adc8"));
        expressionLabel.setAlignment(Pos.CENTER_RIGHT);
        expressionLabel.setMaxWidth(Double.MAX_VALUE);

        display = new TextField("0");
        display.setEditable(false);
        display.setAlignment(Pos.CENTER_RIGHT);
        display.setFont(Font.font("Consolas", FontWeight.BOLD, 32));
        display.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: #cdd6f4; " +
                "-fx-padding: 0;"
        );

        displayBox.getChildren().addAll(expressionLabel, display);

        statusLabel = new Label("Conectando...");
        statusLabel.setFont(Font.font("Segoe UI", 11));
        statusLabel.setTextFill(Color.web("#a6adc8"));
        VBox.setMargin(statusLabel, new Insets(4, 0, 0, 0));

        box.getChildren().addAll(displayBox, statusLabel);
        return box;
    }

    private TabPane buildTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: #1e1e2e;");

        Tab basicTab = new Tab("  Básica  ", buildBasicGrid());
        Tab sciTab   = new Tab("  Científica  ", buildScientificGrid());
        Tab convTab  = new Tab("  Conversão  ", buildConversionPanel());

        tabPane.getTabs().addAll(basicTab, sciTab, convTab);
        return tabPane;
    }

    private GridPane buildBasicGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));
        grid.setStyle("-fx-background-color: #1e1e2e;");

        String[][] buttons = {
                {"C",   "±",   "%",   "÷"},
                {"7",   "8",   "9",   "×"},
                {"4",   "5",   "6",   "−"},
                {"1",   "2",   "3",   "+"},
                {"0",   ".",   "⌫",  "="}
        };

        for (int row = 0; row < buttons.length; row++) {
            for (int col = 0; col < buttons[row].length; col++) {
                String label = buttons[row][col];
                Button btn = createButton(label, getButtonStyle(label));
                btn.setOnAction(e -> handleBasicInput(label));
                grid.add(btn, col, row);
            }
        }
        return grid;
    }

    private GridPane buildScientificGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));
        grid.setStyle("-fx-background-color: #1e1e2e;");

        String[][] buttons = {
                {"xʸ",  "ʸ√x",  "n!",  "mod"},
                {"7",   "8",    "9",   "÷"},
                {"4",   "5",    "6",   "×"},
                {"1",   "2",    "3",   "−"},
                {"0",   ".",    "⌫",  "="},
                {"C",   "±",    "%",   "+"}
        };

        for (int row = 0; row < buttons.length; row++) {
            for (int col = 0; col < buttons[row].length; col++) {
                String label = buttons[row][col];
                Button btn = createButton(label, getButtonStyle(label));
                btn.setOnAction(e -> handleScientificInput(label));
                grid.add(btn, col, row);
            }
        }
        return grid;
    }

    private VBox buildConversionPanel() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: #1e1e2e;");

        Label title = new Label("Conversão de Bases Numéricas");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#cdd6f4"));

        TextField inputField = new TextField();
        inputField.setPromptText("Digite o valor...");
        inputField.setStyle(fieldStyle());

        ComboBox<String> fromBase = new ComboBox<>();
        fromBase.getItems().addAll("Decimal", "Binário", "Hexadecimal");
        fromBase.setValue("Decimal");
        fromBase.setStyle(comboStyle());

        ComboBox<String> toBase = new ComboBox<>();
        toBase.getItems().addAll("Decimal", "Binário", "Hexadecimal");
        toBase.setValue("Binário");
        toBase.setStyle(comboStyle());

        HBox convRow = new HBox(10, new Label("De:"), fromBase,
                new Label("Para:"), toBase);
        convRow.setAlignment(Pos.CENTER_LEFT);
        styleLabelsInHBox(convRow);

        Button convertBtn = createButton("Converter", accentStyle());
        convertBtn.setMaxWidth(Double.MAX_VALUE);

        Label resultLabel = new Label("Resultado: —");
        resultLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        resultLabel.setTextFill(Color.web("#a6e3a1"));

        convertBtn.setOnAction(e -> {
            String val = inputField.getText().trim();
            if (val.isEmpty() || calc == null) return;
            try {
                int fromInt = getBaseFromStr(fromBase.getValue());
                int toInt = getBaseFromStr(toBase.getValue());
                
                String formatted = calc.convertBase(val, fromInt, toInt);
                resultLabel.setText("Resultado: " + formatted);
                display.setText(formatted);
                expressionLabel.setText(fromBase.getValue() + " -> " + toBase.getValue() + " (" + val + ")");
                refreshHistory();
            } catch (NumberFormatException ex) {
                resultLabel.setText("Entrada inválida.");
            } catch (RemoteException ex) {
                resultLabel.setText("Erro remoto.");
            }
        });

        box.getChildren().addAll(title, inputField, convRow, convertBtn, resultLabel);
        return box;
    }

    private int getBaseFromStr(String base) {
        return switch (base) {
            case "Binário" -> 2;
            case "Hexadecimal" -> 16;
            default -> 10;
        };
    }

    private void handleBasicInput(String label) {
        switch (label) {
            case "C"  -> clear();
            case "⌫" -> backspace();
            case "±"  -> negate();
            case "="  -> computeResult();
            case "+"  -> setOperator("+");
            case "−"  -> setOperator("−");
            case "×"  -> setOperator("×");
            case "÷"  -> setOperator("÷");
            case "%"  -> applyPercent();
            default   -> appendDigit(label);
        }
    }

    private void handleScientificInput(String label) {
        switch (label) {
            case "xʸ"  -> setOperator("xʸ");
            case "ʸ√x" -> setOperator("ʸ√x");
            case "n!"  -> applySingle("n!");
            case "mod" -> setOperator("mod");
            default    -> handleBasicInput(label);
        }
    }

    private void appendDigit(String d) {
        if (newInput) { currentInput = ""; newInput = false; }
        if (d.equals(".") && currentInput.contains(".")) return;
        if (currentInput.equals("0") && !d.equals(".")) currentInput = d;
        else currentInput += d;
        display.setText(currentInput);
    }

    private void setOperator(String op) {
        firstOperand = parseDisplayValue();
        operator = op;
        newInput = true;
        expressionLabel.setText(formatNumber(firstOperand) + " " + operator);
    }

    private void computeResult() {
        if (operator.isEmpty() || calc == null) return;
        double second = parseDisplayValue();
        try {
            expressionLabel.setText(formatNumber(firstOperand) + " " + operator + " " + formatNumber(second) + " =");
            Number result = switch (operator) {
                case "+"    -> calc.sum(firstOperand, second);
                case "−"    -> calc.sub(firstOperand, second);
                case "×"    -> calc.mul(firstOperand, second);
                case "÷"    -> calc.div(firstOperand, second);
                case "xʸ"  -> calc.pow(second, firstOperand);
                case "ʸ√x" -> calc.root(second, firstOperand);
                case "mod"  -> calc.mod(firstOperand, second);
                default     -> null;
            };
            if (result != null) {
                display.setText(formatNumber(result));
                currentInput = display.getText();
            }
            refreshHistory();
        } catch (RemoteException e) {
            display.setText("Erro RMI");
        }
        operator = "";
        newInput = true;
    }

    private void applySingle(String op) {
        if (calc == null) return;
        double val = parseDisplayValue();
        try {
            expressionLabel.setText(op + "(" + formatNumber(val) + ") =");
            Number result = switch (op) {
                case "n!" -> calc.factorial(val);
                default          -> null;
            };
            if (result != null) {
                display.setText(formatNumber(result));
                currentInput = display.getText();
            }
            refreshHistory();
        } catch (RemoteException e) {
            display.setText("Erro RMI");
        }
        newInput = true;
    }

    private void applyPercent() {
        if (calc == null) return;
        try {
            double second = parseDisplayValue();
            expressionLabel.setText(formatNumber(firstOperand == 0 ? second : firstOperand) + " % " + formatNumber(second) + " =");
            Number result = calc.percent(firstOperand == 0 ? second : firstOperand, second);
            display.setText(formatNumber(result));
            currentInput = display.getText();
            refreshHistory();
        } catch (RemoteException e) {
            display.setText("Erro RMI");
        }
        newInput = true;
    }

    private void clear() {
        currentInput = "0"; operator = ""; firstOperand = 0; newInput = true;
        display.setText("0");
        expressionLabel.setText("");
    }

    private void backspace() {
        if (currentInput.length() > 1)
            currentInput = currentInput.substring(0, currentInput.length() - 1);
        else currentInput = "0";
        display.setText(currentInput);
    }

    private void negate() {
        double val = parseDisplayValue() * -1;
        currentInput = formatNumber(val);
        display.setText(currentInput);
    }

    private VBox buildHistoryPanel() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        box.setPrefWidth(190);
        box.setStyle("-fx-background-color: #181825; -fx-border-color: #45475a; -fx-border-width: 0 0 0 1;");

        Label title = new Label("Histórico");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        title.setTextFill(Color.web("#cdd6f4"));

        historyArea = new TextArea();
        historyArea.setEditable(false);
        historyArea.setWrapText(true);
        historyArea.setFont(Font.font("Consolas", 11));
        historyArea.setStyle(
                "-fx-control-inner-background: #1e1e2e; " +
                        "-fx-text-fill: #a6adc8; " +
                        "-fx-border-color: transparent;"
        );
        VBox.setVgrow(historyArea, Priority.ALWAYS);

        Button refreshBtn = createButton("↻ Atualizar", secondaryStyle());
        refreshBtn.setMaxWidth(Double.MAX_VALUE);
        refreshBtn.setOnAction(e -> refreshHistory(refreshBtn));

        box.getChildren().addAll(title, historyArea, refreshBtn);
        return box;
    }

    private void refreshHistory() {
        refreshHistory(null);
    }

    private void refreshHistory(Button btn) {
        if (calc == null) return;
        
        if (btn != null) {
            btn.setText("Atualizando...");
            btn.setDisable(true);
        }
        
        new Thread(() -> {
            try {
                List<String> ops = calc.lastOperations(10);
                StringBuilder sb = new StringBuilder();
                for (int i = ops.size() - 1; i >= 0; i--)
                    sb.append(ops.get(i)).append("\n\n");
                
                String newText = sb.toString();
                Platform.runLater(() -> {
                    historyArea.setText(newText);
                    if (btn != null) {
                        btn.setText("↻ Atualizar");
                        btn.setDisable(false);
                    }
                });
            } catch (RemoteException e) {
                Platform.runLater(() -> {
                    historyArea.setText("Erro ao carregar histórico.");
                    if (btn != null) {
                        btn.setText("↻ Atualizar");
                        btn.setDisable(false);
                    }
                });
            }
        }).start();
    }

    private double parseDisplayValue() {
        try { return Double.parseDouble(display.getText()); }
        catch (NumberFormatException e) { return 0; }
    }

    private String formatNumber(Number n) {
        double v = n.doubleValue();
        if (v == Math.floor(v) && !Double.isInfinite(v))
            return String.valueOf((long) v);
        return String.valueOf(v);
    }

    private void updateStatus(String msg, boolean ok) {
        if (statusLabel != null) {
            statusLabel.setText(ok ? "✔ " + msg : "✘ " + msg);
            statusLabel.setTextFill(ok ? Color.web("#a6e3a1") : Color.web("#f38ba8"));
        }
    }

    private Button createButton(String text, String style) {
        Button btn = new Button(text);
        btn.setMinSize(64, 48);
        btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        btn.setStyle(style);
        btn.setOnMouseEntered(e -> btn.setStyle(style + " -fx-opacity: 0.8;"));
        btn.setOnMouseExited(e  -> btn.setStyle(style));
        return btn;
    }

    private String getButtonStyle(String label) {
        return switch (label) {
            case "=" -> accentStyle();
            case "C" -> "-fx-background-color: #f38ba8; -fx-text-fill: #1e1e2e; " +
                    "-fx-background-radius: 8; -fx-border-radius: 8;";
            case "+", "−", "×", "÷", "mod", "xʸ", "ʸ√x", "n!" ->
                    "-fx-background-color: #89b4fa; -fx-text-fill: #1e1e2e; " +
                            "-fx-background-radius: 8; -fx-border-radius: 8;";
            default  -> defaultBtnStyle();
        };
    }

    private String accentStyle() {
        return "-fx-background-color: #a6e3a1; -fx-text-fill: #1e1e2e; " +
                "-fx-background-radius: 8; -fx-border-radius: 8;";
    }

    private String defaultBtnStyle() {
        return "-fx-background-color: #313244; -fx-text-fill: #cdd6f4; " +
                "-fx-background-radius: 8; -fx-border-radius: 8;";
    }

    private String secondaryStyle() {
        return "-fx-background-color: #45475a; -fx-text-fill: #cdd6f4; " +
                "-fx-background-radius: 8; -fx-border-radius: 8;";
    }

    private String fieldStyle() {
        return "-fx-background-color: #2a2a3e; -fx-text-fill: #cdd6f4; " +
                "-fx-border-color: #45475a; -fx-border-radius: 6; " +
                "-fx-background-radius: 6; -fx-padding: 6 10;";
    }

    private String comboStyle() {
        return "-fx-background-color: #2a2a3e; -fx-text-fill: #cdd6f4; " +
                "-fx-border-color: #45475a; -fx-border-radius: 6;";
    }

    private void styleLabelsInHBox(HBox box) {
        box.getChildren().stream()
                .filter(n -> n instanceof Label)
                .forEach(n -> {
                    ((Label) n).setTextFill(Color.web("#a6adc8"));
                    ((Label) n).setFont(Font.font("Segoe UI", 13));
                });
    }

    public static void main(String[] args) {
        launch(args);
    }
}