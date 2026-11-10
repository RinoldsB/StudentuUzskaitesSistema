package studentuuzskaitesistema.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import studentuuzskaitesistema.dao.GradeDAO;
import studentuuzskaitesistema.dao.GroupDAO;
import studentuuzskaitesistema.dao.StudentDAO;
import studentuuzskaitesistema.dao.UserDAO;
import studentuuzskaitesistema.model.Grade;
import studentuuzskaitesistema.model.Student;
import studentuuzskaitesistema.model.StudentGroup;
import studentuuzskaitesistema.model.User;
import studentuuzskaitesistema.util.Session;
import studentuuzskaitesistema.util.Validator;

/**
 * Lietotnes grafiskās lietotāja saskarnes galvenā klase (pieslēgšanās logs un
 * apakšlogi: reģistrācija, galvenā izvēlne, studenti, grupas, sekmes, atskaite un palīdzība).
 *
 * @author Rinolds Baikovs, Antons Liepa
 */
public class LoginFrame extends javax.swing.JFrame {

    private List<StudentGroup> loadedGroups = new ArrayList<>();
    private List<StudentGroup> filterGroups = new ArrayList<>();
    private List<User> loadedTeachers = new ArrayList<>();
    private List<Student> loadedGradeStudents = new ArrayList<>();
    private List<StudentGroup> reportGroups = new ArrayList<>();
    private List<Student> reportStudents = new ArrayList<>();

    private JTextField txtSearchStudent;
    private JComboBox<String> cmbFilterGroup;
    private JButton btnSearchStudent;
    private JButton btnResetSearch;
    private JTextField txtGradeDate;
    private JLabel lblLoggedUser;
    private JButton btnReport;
    private JButton btnHelp;

    private JDialog ReportDialog;
    private JComboBox<String> cmbReportGroup;
    private JComboBox<String> cmbReportStudent;
    private JTable tblReport;
    private JLabel lblGroupAverage;

    /**
     * Izveido pieslēgšanās logu un inicializē visus apakšlogus.
     */
    public LoginFrame() {
        initComponents();
        initCustomComponents();
        setLocationRelativeTo(null);
    }

    /**
     * Noņem visus iepriekšējos klausītājus no pogas un pievieno tieši vienu {@link java.awt.event.ActionListener},
     * kā arī nodrošina, ka poga ir aktīva un fokusējama.
     */
    private void setSingleActionListener(javax.swing.AbstractButton btn, java.awt.event.ActionListener listener) {
        if (btn == null) {
            return;
        }
        for (java.awt.event.ActionListener al : btn.getActionListeners()) {
            btn.removeActionListener(al);
        }
        for (java.awt.event.MouseListener ml : btn.getMouseListeners()) {
            if (ml.getClass().getName().contains("LoginFrame")) {
                btn.removeMouseListener(ml);
            }
        }
        btn.setEnabled(true);
        btn.setFocusable(true);
        btn.addActionListener(listener);
    }

    /**
     * Konfigurē teksta ievadlaukus, lai tajos vienmēr varētu brīvi rakstīt un tie nekad nesaruktu.
     */
    private void configureTextField(JTextField field, int columns, int minWidth) {
        if (field == null) {
            return;
        }
        field.setEditable(true);
        field.setEnabled(true);
        field.setFocusable(true);
        field.setColumns(columns);
        java.awt.Dimension minDim = new java.awt.Dimension(minWidth, 28);
        java.awt.Dimension prefDim = new java.awt.Dimension(Math.max(minWidth, columns * 11), 28);
        field.setMinimumSize(minDim);
        field.setPreferredSize(prefDim);
        String current = field.getText();
        if (current != null && current.matches("jTextField\\d+|jPasswordField\\d+")) {
            field.setText("");
        }
    }

    /**
     * Papildina un pārkārto visus dialogus, nodrošinot ka katrs ievadlauks un katra poga darbojas korekti.
     */
    private void initCustomComponents() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        for (java.awt.event.WindowListener wl : getWindowListeners()) {
            if (wl.getClass().getName().contains("LoginFrame")) {
                removeWindowListener(wl);
            }
        }
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExitApplication();
            }
        });

        // 1. Konfigurē visus teksta laukus, lai tajos vienmēr varētu brīvi rakstīt.
        configureTextField(txtUsername, 16, 180);
        configureTextField(txtPassword, 16, 180);
        configureTextField(txtFullName, 18, 200);
        configureTextField(txtRegUsername, 18, 200);
        configureTextField(txtRegPassword, 18, 200);
        configureTextField(txtFirstName, 14, 160);
        configureTextField(txtLastName, 14, 160);
        configureTextField(txtPersonalCode, 14, 160);
        configureTextField(txtEmail, 16, 180);
        configureTextField(txtPhone, 16, 180);
        configureTextField(txtGroupName, 20, 240);
        configureTextField(txtGrade, 8, 100);

        txtSearchStudent = new JTextField();
        configureTextField(txtSearchStudent, 16, 170);
        txtGradeDate = new JTextField(LocalDate.now().toString());
        configureTextField(txtGradeDate, 10, 120);
        txtGradeDate.setText(LocalDate.now().toString());

        // Enter taustiņa nospiešana pieslēgšanās un meklēšanas laukos
        setSingleTextFieldAction(txtUsername, this::btnLoginActionPerformed);
        setSingleTextFieldAction(txtPassword, this::btnLoginActionPerformed);
        setSingleTextFieldAction(txtRegPassword, this::btnSaveActionPerformed);
        setSingleTextFieldAction(txtSearchStudent, evt -> performStudentSearch(true));

        // 2. Etiķešu pareizrakstības labojums
        jLabel1.setText("Parole:");
        jLabel2.setText("Lietotājvārds:");
        jLabel6.setText("Vārds:");
        jLabel10.setText("Grupa:");
        jLabel18.setText("Atzīme (1–10):");

        // 3. Jaunās pogas un komponentes
        lblLoggedUser = new JLabel("Pieslēdzies kā: -");
        btnReport = new JButton("Atskaite");
        btnHelp = new JButton("Palīdzība");
        cmbFilterGroup = new JComboBox<>(new String[]{"Visas"});
        btnSearchStudent = new JButton("Meklēt");
        btnResetSearch = new JButton("Rādīt visus");

        // 4. Tabulu modeļu un rediģēšanas iestatījumi (lai dubultklikšķis neieslēgtu šūnas rediģēšanu)
        tblStudents.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Vārds", "Uzvārds", "Personas kods", "E-pasts", "Tālrunis", "Grupa"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        tblStudents.setDefaultEditor(Object.class, null);

        tblGroups.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Grupas nosaukums", "Kurss", "Pasniedzējs"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        tblGroups.setDefaultEditor(Object.class, null);

        tblGroups1.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Students", "Priekšmets", "Atzīme", "Datums"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        tblGroups1.setDefaultEditor(Object.class, null);

        // 5. Pievieno katrai pogai tieši vienu ActionListener
        setSingleActionListener(btnLogin, this::btnLoginActionPerformed);
        setSingleActionListener(btnRegister, this::btnRegisterActionPerformed);
        setSingleActionListener(btnSave, this::btnSaveActionPerformed);
        setSingleActionListener(btnCancel, this::btnCancelActionPerformed);

        setSingleActionListener(btnStudents, this::btnStudentsActionPerformed);
        setSingleActionListener(btnGroups, this::btnGroupsActionPerformed);
        setSingleActionListener(btnGrades, evt -> openGradeDialog());
        setSingleActionListener(btnReport, evt -> openReportDialog());
        setSingleActionListener(btnHelp, evt -> showHelpDialog());
        setSingleActionListener(btnLogout, this::btnLogoutActionPerformed);

        setSingleActionListener(btnSearchStudent, evt -> performStudentSearch(true));
        setSingleActionListener(btnResetSearch, evt -> {
            txtSearchStudent.setText("");
            if (cmbFilterGroup.getItemCount() > 0) {
                cmbFilterGroup.setSelectedIndex(0);
            }
            performStudentSearch(false);
        });
        setSingleActionListener(btnAddStudent, this::btnAddStudentActionPerformed);
        setSingleActionListener(btnUpdateStudent, this::btnUpdateStudentActionPerformed);
        setSingleActionListener(btnDeleteStudent, this::btnDeleteStudentActionPerformed);
        setSingleActionListener(btnClearStudent, this::btnClearStudentActionPerformed);
        setSingleActionListener(btnBack, this::btnBackActionPerformed);

        setSingleActionListener(btnAddStudent1, this::btnAddStudent1ActionPerformed);
        setSingleActionListener(btnUpdateStudent1, this::btnUpdateStudent1ActionPerformed);
        setSingleActionListener(btnDeleteStudent1, this::btnDeleteStudent1ActionPerformed);
        setSingleActionListener(btnClearStudent1, this::btnClearStudent1ActionPerformed);
        setSingleActionListener(btnBack1, this::btnBack1ActionPerformed);

        setSingleActionListener(btnAddStudent2, this::btnAddStudent2ActionPerformed);
        setSingleActionListener(btnUpdateStudent2, this::btnUpdateStudent2ActionPerformed);
        setSingleActionListener(btnDeleteStudent2, this::btnDeleteStudent2ActionPerformed);
        setSingleActionListener(btnClearStudent2, this::btnClearStudent2ActionPerformed);
        setSingleActionListener(btnBack2, this::btnBack2ActionPerformed);

        // 6. Pārkārto visus logus ar stabiliem izkārtojumiem
        rebuildLoginLayout();
        rebuildRegisterDialogLayout();
        rebuildMainMenuLayout();
        rebuildStudentDialogLayout();
        rebuildGroupDialogLayout();
        rebuildGradeDialogLayout();
        buildReportDialog();

        // 7. Tabulu rindu izvēles klausītāji
        tblStudents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblStudents.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateStudentFieldsFromSelectedRow();
            }
        });

        tblGroups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblGroups.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateGroupFieldsFromSelectedRow();
            }
        });

        tblGroups1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblGroups1.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateGradeFieldsFromSelectedRow();
            }
        });
    }

    private void setSingleTextFieldAction(JTextField field, java.awt.event.ActionListener listener) {
        for (java.awt.event.ActionListener al : field.getActionListeners()) {
            field.removeActionListener(al);
        }
        field.addActionListener(listener);
    }

    /**
     * Izveido ērtu un pārskatāmu pieslēgšanās loga izkārtojumu.
     */
    private void rebuildLoginLayout() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(20, 28, 16, 28));

        JLabel header = new JLabel("Studentu uzskaites sistēma", javax.swing.SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 16));
        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(jLabel2, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(jLabel1, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtPassword, gbc);

        root.add(form, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout(8, 8));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        btnPanel.add(btnRegister);
        btnPanel.add(btnLogin);
        southPanel.add(btnPanel, BorderLayout.NORTH);

        JLabel hintLabel = new JLabel("Noklusējuma konti: admin / admin123  vai  pasniedzejs / pasn123", javax.swing.SwingConstants.CENTER);
        hintLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        southPanel.add(hintLabel, BorderLayout.SOUTH);

        root.add(southPanel, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setMinimumSize(new java.awt.Dimension(420, 240));
    }

    /**
     * Izveido stabilu reģistrācijas dialoga izkārtojumu, kur visi ievadlauki ir pilnā platumā.
     */
    private void rebuildRegisterDialogLayout() {
        RegisterDialog.setTitle("Reģistrācija");
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(jLabel3, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtFullName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(jLabel4, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtRegUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(jLabel5, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtRegPassword, gbc);

        root.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        root.add(btnPanel, BorderLayout.SOUTH);

        RegisterDialog.setContentPane(root);
        RegisterDialog.pack();
        RegisterDialog.setMinimumSize(new java.awt.Dimension(420, 240));
    }

    /**
     * Pārkārto galvenās izvēlnes logu, pievienojot atskaites un palīdzības pogas un statusa joslu.
     */
    private void rebuildMainMenuLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 40;
        gbc.ipady = 12;
        gbc.gridx = 0;

        gbc.gridy = 0;
        buttonsPanel.add(btnStudents, gbc);
        gbc.gridy = 1;
        buttonsPanel.add(btnGroups, gbc);
        gbc.gridy = 2;
        buttonsPanel.add(btnGrades, gbc);
        gbc.gridy = 3;
        buttonsPanel.add(btnReport, gbc);
        gbc.gridy = 4;
        buttonsPanel.add(btnHelp, gbc);

        mainPanel.add(buttonsPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.add(lblLoggedUser, BorderLayout.WEST);
        bottomPanel.add(btnLogout, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        MainMenuDialog.setContentPane(mainPanel);
        MainMenuDialog.pack();
        MainMenuDialog.setMinimumSize(new java.awt.Dimension(480, 400));
        MainMenuDialog.setSize(480, 400);
    }

    /**
     * Pārkārto StudentDialog ar meklēšanas joslu un pilna platuma ievadlaukiem (FP-01, FP-04, FP-05, FP-06).
     */
    private void rebuildStudentDialogLayout() {
        StudentDialog.setTitle("Studenti");
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel topContainer = new JPanel(new BorderLayout(8, 8));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        searchBar.setBorder(BorderFactory.createTitledBorder("Meklēšana un filtrēšana"));
        searchBar.add(new JLabel("Meklēt (vārds, uzvārds, pers. kods):"));
        searchBar.add(txtSearchStudent);
        searchBar.add(new JLabel("Grupa:"));
        searchBar.add(cmbFilterGroup);
        searchBar.add(btnSearchStudent);
        searchBar.add(btnResetSearch);
        topContainer.add(searchBar, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Studenta dati"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // 1. rinda: Vārds un E-pasts
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(jLabel6, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtFirstName, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(jLabel9, gbc);

        gbc.gridx = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtEmail, gbc);

        // 2. rinda: Uzvārds un Tālrunis
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(jLabel7, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtLastName, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(jLabel11, gbc);

        gbc.gridx = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtPhone, gbc);

        // 3. rinda: Personas kods un Grupa
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(jLabel8, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtPersonalCode, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(jLabel10, gbc);

        gbc.gridx = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(cmbGroup, gbc);

        topContainer.add(formPanel, BorderLayout.CENTER);
        root.add(topContainer, BorderLayout.NORTH);

        jScrollPane1.setViewportView(tblStudents);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(700, 220));
        root.add(jScrollPane1, BorderLayout.CENTER);

        JPanel buttonsBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        java.awt.Dimension btnDim = new java.awt.Dimension(110, 38);
        btnAddStudent.setPreferredSize(btnDim);
        btnUpdateStudent.setPreferredSize(btnDim);
        btnDeleteStudent.setPreferredSize(btnDim);
        btnClearStudent.setPreferredSize(btnDim);
        btnBack.setPreferredSize(btnDim);
        buttonsBar.add(btnAddStudent);
        buttonsBar.add(btnUpdateStudent);
        buttonsBar.add(btnDeleteStudent);
        buttonsBar.add(btnClearStudent);
        buttonsBar.add(btnBack);
        root.add(buttonsBar, BorderLayout.SOUTH);

        StudentDialog.setContentPane(root);
        StudentDialog.pack();
        StudentDialog.setMinimumSize(new java.awt.Dimension(760, 520));
    }

    /**
     * Pārkārto GroupDialog ar stabilu izkārtojumu (FP-03, FP-04, FP-05, FP-11).
     */
    private void rebuildGroupDialogLayout() {
        GroupDialog.setTitle("Grupas");
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Grupas dati"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(jLabel12, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtGroupName, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(jLabel13, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(cmbCourse, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(jLabel15, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(cmbTeacher, gbc);

        root.add(formPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(4, 4));
        centerPanel.add(jLabel14, BorderLayout.NORTH);
        jScrollPane2.setViewportView(tblGroups);
        jScrollPane2.setPreferredSize(new java.awt.Dimension(560, 200));
        centerPanel.add(jScrollPane2, BorderLayout.CENTER);
        root.add(centerPanel, BorderLayout.CENTER);

        JPanel buttonsBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        java.awt.Dimension btnDim = new java.awt.Dimension(105, 38);
        btnAddStudent1.setPreferredSize(btnDim);
        btnUpdateStudent1.setPreferredSize(btnDim);
        btnDeleteStudent1.setPreferredSize(btnDim);
        btnClearStudent1.setPreferredSize(btnDim);
        btnBack1.setPreferredSize(btnDim);
        buttonsBar.add(btnAddStudent1);
        buttonsBar.add(btnUpdateStudent1);
        buttonsBar.add(btnDeleteStudent1);
        buttonsBar.add(btnClearStudent1);
        buttonsBar.add(btnBack1);
        root.add(buttonsBar, BorderLayout.SOUTH);

        GroupDialog.setContentPane(root);
        GroupDialog.pack();
        GroupDialog.setMinimumSize(new java.awt.Dimension(620, 450));
    }

    /**
     * Pārkārto GradeDialog ar datuma ievades lauku un pilna platuma komponentēm (FP-07, FP-08).
     */
    private void rebuildGradeDialogLayout() {
        GradeDialog.setTitle("Atzīmes");
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Sekmes dati"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // 1. rinda: Students un Atzīme
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(jLabel16, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(cmbStudent, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(jLabel18, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtGrade, gbc);

        // 2. rinda: Priekšmets un Datums
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(jLabel17, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(cmbSubject, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Datums (GGGG-MM-DD):"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtGradeDate, gbc);

        root.add(formPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(4, 4));
        centerPanel.add(jLabel19, BorderLayout.NORTH);
        jScrollPane3.setViewportView(tblGroups1);
        jScrollPane3.setPreferredSize(new java.awt.Dimension(640, 210));
        centerPanel.add(jScrollPane3, BorderLayout.CENTER);
        root.add(centerPanel, BorderLayout.CENTER);

        JPanel buttonsBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        java.awt.Dimension btnDim = new java.awt.Dimension(105, 38);
        btnAddStudent2.setPreferredSize(btnDim);
        btnUpdateStudent2.setPreferredSize(btnDim);
        btnDeleteStudent2.setPreferredSize(btnDim);
        btnClearStudent2.setPreferredSize(btnDim);
        btnBack2.setPreferredSize(btnDim);
        buttonsBar.add(btnAddStudent2);
        buttonsBar.add(btnUpdateStudent2);
        buttonsBar.add(btnDeleteStudent2);
        buttonsBar.add(btnClearStudent2);
        buttonsBar.add(btnBack2);
        root.add(buttonsBar, BorderLayout.SOUTH);

        GradeDialog.setContentPane(root);
        GradeDialog.pack();
        GradeDialog.setMinimumSize(new java.awt.Dimension(680, 470));
    }

    /**
     * Izveido sekmju atskaites dialogu (ReportDialog, FP-09).
     */
    private void buildReportDialog() {
        ReportDialog = new JDialog(this, "Sekmju atskaite", true);
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        cmbReportGroup = new JComboBox<>();
        cmbReportStudent = new JComboBox<>();
        JButton btnGenerateReport = new JButton("Rādīt atskaiti");
        JButton btnPrintReport = new JButton("Drukāt");
        JButton btnCloseReport = new JButton("Atpakaļ");

        filterPanel.add(new JLabel("Grupa:"));
        filterPanel.add(cmbReportGroup);
        filterPanel.add(new JLabel("Students:"));
        filterPanel.add(cmbReportStudent);
        filterPanel.add(btnGenerateReport);

        tblReport = new JTable(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Students", "Personas kods", "Grupa", "Sekmju skaits", "Vidējā atzīme"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        lblGroupAverage = new JLabel("Grupas vidējā atzīme: -");
        lblGroupAverage.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel southPanel = new JPanel(new BorderLayout(10, 6));
        southPanel.add(lblGroupAverage, BorderLayout.WEST);
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.add(btnPrintReport);
        actionPanel.add(btnCloseReport);
        southPanel.add(actionPanel, BorderLayout.EAST);

        root.add(filterPanel, BorderLayout.NORTH);
        root.add(new JScrollPane(tblReport), BorderLayout.CENTER);
        root.add(southPanel, BorderLayout.SOUTH);

        cmbReportGroup.addActionListener(evt -> populateReportStudentsCombo());
        btnGenerateReport.addActionListener(evt -> refreshReportTable());
        btnPrintReport.addActionListener(evt -> printReportTable());
        btnCloseReport.addActionListener(evt -> ReportDialog.setVisible(false));

        ReportDialog.setContentPane(root);
        ReportDialog.setSize(680, 420);
        ReportDialog.setLocationRelativeTo(null);
    }

    /**
     * Parāda lietotāja palīdzības logu (Help, 4.2. nodaļa).
     */
    private void showHelpDialog() {
        String helpText =
                "STUDENTU UZSKAITES SISTĒMA – PALĪDZĪBA\n\n"
                + "1. Lietotāju lomas:\n"
                + "   • Lietvedis (ADMIN): pievieno, labo un dzēš studentus un grupas, piešķir grupām pasniedzējus,\n"
                + "     meklē studentus un skata/drukā sekmju atskaites.\n"
                + "   • Pasniedzējs (TEACHER): skata un meklē tikai savu grupu studentus, ievada, labo un dzēš sekmes,\n"
                + "     kā arī sagatavo sekmju atskaiti par savu grupu.\n\n"
                + "2. Darbs ar sadaļām:\n"
                + "   • Studenti: ievadiet vārdu, uzvārdu, personas kodu (123456-12345), izvēlieties grupu un nospiediet «Pievienot».\n"
                + "     Lai labotu vai dzēstu, vispirms izvēlieties rindu tabulā.\n"
                + "   • Meklēšana: ievadiet vārdu, uzvārdu vai personas kodu meklēšanas laukā, izvēlieties grupu un nospiediet «Meklēt».\n"
                + "   • Grupas (tikai lietvedim): ievadiet grupas nosaukumu, kursu un izvēlieties pasniedzēju.\n"
                + "     Grupu nevar dzēst, kamēr tajā ir reģistrēti studenti.\n"
                + "   • Atzīmes: izvēlieties studentu, priekšmetu, ievadiet atzīmi (vesels skaitlis 1–10) un datumu (GGGG-MM-DD).\n"
                + "   • Atskaite: izvēlieties grupu (un pēc vēlēšanās studentu), apskatiet vidējās atzīmes un nospiediet «Drukāt».";

        JOptionPane.showMessageDialog(
                MainMenuDialog.isVisible() ? MainMenuDialog : this,
                helpText,
                "Palīdzība (Help)",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Pieprasa apstiprinājumu pirms programmas aizvēršanas.
     */
    private void confirmExitApplication() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Vai tiešām aizvērt programmu?",
                "Programmas aizvēršana",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        RegisterDialog = new javax.swing.JDialog();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtFullName = new javax.swing.JTextField();
        txtRegUsername = new javax.swing.JTextField();
        txtRegPassword = new javax.swing.JPasswordField();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        MainMenuDialog = new javax.swing.JDialog();
        lblTitle = new javax.swing.JLabel();
        btnStudents = new javax.swing.JButton();
        btnGroups = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        btnGrades = new javax.swing.JButton();
        StudentDialog = new javax.swing.JDialog();
        jLabel6 = new javax.swing.JLabel();
        txtFirstName = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtLastName = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtPersonalCode = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        cmbGroup = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblStudents = new javax.swing.JTable();
        btnBack = new javax.swing.JButton();
        btnAddStudent = new javax.swing.JButton();
        btnClearStudent = new javax.swing.JButton();
        btnUpdateStudent = new javax.swing.JButton();
        btnDeleteStudent = new javax.swing.JButton();
        GroupDialog = new javax.swing.JDialog();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        txtGroupName = new javax.swing.JTextField();
        cmbCourse = new javax.swing.JComboBox<>();
        cmbTeacher = new javax.swing.JComboBox<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblGroups = new javax.swing.JTable();
        btnBack1 = new javax.swing.JButton();
        btnAddStudent1 = new javax.swing.JButton();
        btnClearStudent1 = new javax.swing.JButton();
        btnUpdateStudent1 = new javax.swing.JButton();
        btnDeleteStudent1 = new javax.swing.JButton();
        GradeDialog = new javax.swing.JDialog();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        cmbStudent = new javax.swing.JComboBox<>();
        cmbSubject = new javax.swing.JComboBox<>();
        txtGrade = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblGroups1 = new javax.swing.JTable();
        btnBack2 = new javax.swing.JButton();
        btnAddStudent2 = new javax.swing.JButton();
        btnClearStudent2 = new javax.swing.JButton();
        btnUpdateStudent2 = new javax.swing.JButton();
        btnDeleteStudent2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnLogin = new javax.swing.JButton();
        btnRegister = new javax.swing.JButton();
        txtPassword = new javax.swing.JPasswordField();
        txtUsername = new javax.swing.JTextField();

        RegisterDialog.setTitle("Reģistrācija");

        jLabel3.setText("Vārds un uzvārds:");

        jLabel4.setText("Lietotājvārds:");

        jLabel5.setText("Parole:");

        txtFullName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFullNameActionPerformed(evt);
            }
        });

        btnSave.setText("Saglabāt");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnCancel.setText("Atcelt");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout RegisterDialogLayout = new javax.swing.GroupLayout(RegisterDialog.getContentPane());
        RegisterDialog.getContentPane().setLayout(RegisterDialogLayout);
        RegisterDialogLayout.setHorizontalGroup(
            RegisterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RegisterDialogLayout.createSequentialGroup()
                .addGroup(RegisterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(RegisterDialogLayout.createSequentialGroup()
                        .addGap(82, 82, 82)
                        .addGroup(RegisterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 57, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RegisterDialogLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(RegisterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnSave)
                    .addComponent(txtFullName)
                    .addComponent(txtRegUsername)
                    .addComponent(txtRegPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE))
                .addGap(23, 23, 23))
        );
        RegisterDialogLayout.setVerticalGroup(
            RegisterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RegisterDialogLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(RegisterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtFullName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(RegisterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtRegUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(RegisterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(txtRegPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(54, 54, 54)
                .addGroup(RegisterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(btnCancel))
                .addContainerGap(105, Short.MAX_VALUE))
        );

        MainMenuDialog.setTitle("Galvenā izvēlne");

        lblTitle.setText("Studentu uzskaites sistēma");

        btnStudents.setText("Studenti");
        btnStudents.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnStudentsMouseClicked(evt);
            }
        });
        btnStudents.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStudentsActionPerformed(evt);
            }
        });

        btnGroups.setText("Grupas");
        btnGroups.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnGroupsMouseClicked(evt);
            }
        });
        btnGroups.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGroupsActionPerformed(evt);
            }
        });

        btnLogout.setText("Iziet");
        btnLogout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnLogoutMouseClicked(evt);
            }
        });
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        btnGrades.setText("Atzīmes");
        btnGrades.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnGradesMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout MainMenuDialogLayout = new javax.swing.GroupLayout(MainMenuDialog.getContentPane());
        MainMenuDialog.getContentPane().setLayout(MainMenuDialogLayout);
        MainMenuDialogLayout.setHorizontalGroup(
            MainMenuDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MainMenuDialogLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(115, 115, 115))
            .addGroup(MainMenuDialogLayout.createSequentialGroup()
                .addGap(148, 148, 148)
                .addGroup(MainMenuDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(MainMenuDialogLayout.createSequentialGroup()
                        .addGroup(MainMenuDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btnStudents, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                            .addComponent(btnGroups, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(MainMenuDialogLayout.createSequentialGroup()
                        .addComponent(btnGrades, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                        .addComponent(btnLogout)
                        .addGap(24, 24, 24))))
        );
        MainMenuDialogLayout.setVerticalGroup(
            MainMenuDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MainMenuDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnStudents, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnGroups, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(MainMenuDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(MainMenuDialogLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnGrades, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(57, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MainMenuDialogLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnLogout)
                        .addGap(38, 38, 38))))
        );

        StudentDialog.setTitle("Studenti");

        jLabel6.setText("Vards:");

        txtFirstName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFirstNameActionPerformed(evt);
            }
        });

        jLabel7.setText("Uzvārds:");

        jLabel8.setText("Personas kods:");

        jLabel9.setText("E-pasts:");

        jLabel10.setText("Gruppa:");

        jLabel11.setText("Tālrunis:");

        cmbGroup.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "K-11", "V-11", "K-21", "V-21", "K-31", "V-31", "K-41", "V-41", "KD-11", "KD-21", "KD-31", "KD-41", "P-11", "P-21", "P-31", "P-41", "P-41_M", "PV-11", "Vsm-11", "PR-11", "PR-21", "PR-31", "PR-32", "ATS-31", "PR-41", "PR-42", "ATS-41", "ET-11", "AE-21", "AE-31", "AE-41", "DT-11", "DT-21", "DT-31", "DT-41", "ET-12", "ET-21", "ET-31", "ET-41", "FR-11", "FR-21", "FR-31", "LD-11", "LD-21", "LD-31", "LD-41", "LDV-11", "TS-11", "TS-21", "TS-31", "KS-41", "TS-41", "ESV-11" }));

        tblStudents.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Vārds", "Uzvārds", "Personas kods", "E-pasts", "Tālrunis", "Grupa"
            }
        ));
        jScrollPane1.setViewportView(tblStudents);

        btnBack.setText("Atpakaļ");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        btnAddStudent.setText("Pievienot");
        btnAddStudent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAddStudentMouseClicked(evt);
            }
        });
        btnAddStudent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddStudentActionPerformed(evt);
            }
        });

        btnClearStudent.setText("Notīrīt");
        btnClearStudent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearStudentActionPerformed(evt);
            }
        });

        btnUpdateStudent.setText("Labot");

        btnDeleteStudent.setText("Dzēst");
        btnDeleteStudent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteStudentActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout StudentDialogLayout = new javax.swing.GroupLayout(StudentDialog.getContentPane());
        StudentDialog.getContentPane().setLayout(StudentDialogLayout);
        StudentDialogLayout.setHorizontalGroup(
            StudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, StudentDialogLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(StudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(StudentDialogLayout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPersonalCode, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(StudentDialogLayout.createSequentialGroup()
                        .addGroup(StudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel6))
                        .addGap(40, 40, 40)
                        .addGroup(StudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtLastName)
                            .addComponent(txtFirstName))))
                .addGap(52, 52, 52)
                .addGroup(StudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(StudentDialogLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(29, 29, 29)
                        .addComponent(txtEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE))
                    .addGroup(StudentDialogLayout.createSequentialGroup()
                        .addGroup(StudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(jLabel10))
                        .addGap(26, 26, 26)
                        .addGroup(StudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPhone, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                            .addComponent(cmbGroup, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(85, 85, 85))
            .addGroup(StudentDialogLayout.createSequentialGroup()
                .addGroup(StudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(StudentDialogLayout.createSequentialGroup()
                        .addGap(127, 127, 127)
                        .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)
                        .addComponent(btnClearStudent, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(StudentDialogLayout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addComponent(btnAddStudent, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(45, 45, 45)
                        .addComponent(btnDeleteStudent, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(btnUpdateStudent, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(StudentDialogLayout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        StudentDialogLayout.setVerticalGroup(
            StudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(StudentDialogLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(StudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(StudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtLastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(StudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtPersonalCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(cmbGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(StudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddStudent, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteStudent, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdateStudent, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(StudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClearStudent, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(119, 119, 119))
        );

        GroupDialog.setTitle("Grupas");

        jLabel12.setText("Grupas nosaukums:");

        jLabel13.setText("Kurss:");

        jLabel14.setText("Grupu saraksts:");

        jLabel15.setText("Pasniedzējs:");

        txtGroupName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtGroupNameActionPerformed(evt);
            }
        });

        cmbCourse.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4" }));

        cmbTeacher.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Nonna Akopova", "Jeļena Aleksejeva", "Ilona Antoneviča", "Inta Baika", "Inna Barkova", "Olga Bazarova", "Svetlana Beļakova", "Anda Bernāne", "Dmitrijs Bogdanovs", "Gabriela Cimmermane", "Žaneta Dalecka", "Anželika Dorofejeva", "Irēna Dortiņa", "Andrejs Dortiņš", "Ilze Drozdovska", "Mihails Fjodorovs", "Olga Gaujeniete", "Aleksejs Gorbuls", "Anna Gurtā", "Jeļena Ivanova", "Olga Iznova", "Mihails Jenbajevs", "Jeļena Jurtajeva", "Inita Karabekova", "Oksana Kartašova", "Daniels Kligulis", "Anastasija Kočmarjova", "Ilona Kokina", "Alfrēds Kokins", "Ēdvins Kovaļovs", "Igors Kovaļovs", "Alla Kozočkina", "Kristīne Krukovska", "Jeļena Krupska", "Jevģenijs Krupskis", "Ināra Kuharonoka", "Tatjana Ļebedeva", "Kirils Ļebedevs", "Dina Ļecka", "Ineta Litauniece", "Jūlija Makņa", "Anželika Mihailova", "Gaļina Minčenko", "Jevgeņijs Novajonoks", "Ludmila Ņeverova", "Jegors Ņikitins", "Diāna Ogorelova", "Sanita Onckule", "Jeļena Oreha", "Jūlija Orliņa", "Anna Ozerska", "Natālija Panteviča", "Sandris Pārpucs", "Angelika Paškeviča", "Deniss Poliščuks", "Aleksandra Pomitkina", "Oļegs Porietis", "Jekaterina Ribakova", "Ivans Rudzis", "Aleksejs Samohvalovs", "Jeļena Sardiko", "Aleksandrs Settarovs", "Dmitrijs Sirotko", "Ērika Skladova", "Rita Stepanova", "Iveta Suseja", "Grigorijs Suhovilo", "Žanna Špoģe", "Inna Titova", "Inese Šulina", "Maija Vasiļevska", "Elita Vanaģele", "Andrejs Vlasenko", "Jurijs Vībe", "Anna Vološina", "Oksana Zuboviča", "Nataļja Žemčugova", "Jūlija Žerebkova" }));
        cmbTeacher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTeacherActionPerformed(evt);
            }
        });

        tblGroups.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "Grupas nosaukums", "Kurss", "Pasniedzējs"
            }
        ));
        jScrollPane2.setViewportView(tblGroups);

        btnBack1.setText("Atpakaļ");
        btnBack1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBack1ActionPerformed(evt);
            }
        });

        btnAddStudent1.setText("Pievienot");
        btnAddStudent1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddStudent1ActionPerformed(evt);
            }
        });

        btnClearStudent1.setText("Notīrīt");
        btnClearStudent1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearStudent1ActionPerformed(evt);
            }
        });

        btnUpdateStudent1.setText("Labot");
        btnUpdateStudent1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateStudent1ActionPerformed(evt);
            }
        });

        btnDeleteStudent1.setText("Dzēst");
        btnDeleteStudent1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteStudent1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout GroupDialogLayout = new javax.swing.GroupLayout(GroupDialog.getContentPane());
        GroupDialog.getContentPane().setLayout(GroupDialogLayout);
        GroupDialogLayout.setHorizontalGroup(
            GroupDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(GroupDialogLayout.createSequentialGroup()
                .addGroup(GroupDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(GroupDialogLayout.createSequentialGroup()
                        .addGap(61, 61, 61)
                        .addGroup(GroupDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 436, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(GroupDialogLayout.createSequentialGroup()
                                .addGap(74, 74, 74)
                                .addComponent(btnBack1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(36, 36, 36)
                                .addComponent(btnClearStudent1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(GroupDialogLayout.createSequentialGroup()
                                .addComponent(btnAddStudent1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(45, 45, 45)
                                .addComponent(btnDeleteStudent1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(33, 33, 33)
                                .addComponent(btnUpdateStudent1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(GroupDialogLayout.createSequentialGroup()
                        .addGap(112, 112, 112)
                        .addGroup(GroupDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(cmbCourse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(74, 74, 74)
                        .addGroup(GroupDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addGroup(GroupDialogLayout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(cmbTeacher, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(GroupDialogLayout.createSequentialGroup()
                        .addGap(75, 75, 75)
                        .addComponent(jLabel12))
                    .addGroup(GroupDialogLayout.createSequentialGroup()
                        .addGap(91, 91, 91)
                        .addComponent(txtGroupName, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        GroupDialogLayout.setVerticalGroup(
            GroupDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(GroupDialogLayout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtGroupName, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(GroupDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(GroupDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbCourse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbTeacher, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(GroupDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddStudent1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteStudent1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdateStudent1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(GroupDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBack1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClearStudent1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(66, Short.MAX_VALUE))
        );

        GradeDialog.setTitle("Atzīmes");

        jLabel16.setText("Students:");

        jLabel17.setText("Priekšmets:");

        jLabel18.setText("Atzīme:");

        jLabel19.setText("Atzīmju saraksts:");

        cmbStudent.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cmbSubject.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Aktīvā tūrisma pakalpojumi", "Automātiskās lokomotīvju signalizācijas sistēmas borta ierīču apkalpošana", "Audzināšanas stunda ar psihologu", "Algoritmēšanas un programmēšanas pamati", "Ārējo vadu un bezvadu telekomunikāciju tīklu..", "Bāra uzkodu un sagatavju gatavošana", "Biroja darba nodrošināšana", "Budžeta izstrāde un kontrolēšana", "Biznesa plāna izstrāde", "Banketa plānošana", "Biroja tehnikas apkope un remonts", "Darba tiesības", "Darba organizēšana mazumtirdzniecības vietā", "Datu bāzu programmēšana", "Datu bāzu tehnoloģijas", "Dzelzceļa elektrobarošanas līniju un ierīču remonts", "Dzelzceļa elektrobarošanas sistēmas apkalpošana", "Dzelzceļa elektrotehnisko sistēmu projektēšana un būvniecība", "Dzelzceļa kontakttīkla sistēmas apkalpošana", "Dzelzceļa kontakttīkla sistēmas remonts", "Dzelzceļa kontakttīkla sistēmas ierīču uzturēšana", "Darbs mazumtirdzniecības vietā", "Dzelzceļa nozares darbības pamatprocesi", "Dzelzceļa signalizācijas", "centralizācijas un bloķēšanas sistēmu apkalpošana", "Darbs tirdzniecības uzņēmuma noliktavā", "Datorsistēmas un datortīkli", "Dzērienu un kokteiļu gatavošana", "Dabaszinības", "Ēdināšanas uzņēmuma darbības pamatprincipi", "Ēdiena gatavošanas pamatiemaņas", "EIKT drošības politika", "EIKT drošības politikas veidošana", "Ēdienu gatavošanas tehnoloģija (2.līmenis)", "Ēdienu gatavošanas tehnoloģija (1.līmenis)", "Ēdienkartes izveide", "Svešvaloda I (B2) angļu valoda", "EIKT nozares tehnisko darbu pamatiemaņas", "Ekotūrisma pakalpojumi", "EIKT produktu izstrāde", "EIKT pamatprosesi un darbu veidi", "Elektronisko sakaru tīklu tehnoloģijas", "to ierīkošana un uzturēšana", "Elektronikas un elektrotehnikas pamati", "Fizika I", "Galda klāšanas pamati", "Grupas audzinātāja stunda", "Iniciatīva un uzņēmējdarbība (1.", "2.līmenis)", "Informācijas un komunikācijas tehnoloģijas (2.līmenis)", "Informācijas un komunikāc. tehnoloģijas (1.", "2.l.)", "Iniciatīva un uzņēmējdarbība (2.līmenis)", "Izmēģinājuma eksāmens angļu valodā", "Konditorejas izstrādājumu gatavošana (1.līmenis)", "Konditorejas izstrādājumu gatavošana (2.līmenis)", "Konditora palīga prakse", "Konferenču un semināru apkalpošana", "Konsultācija pirms PKE", "Konditora prakse", "Konsultācija pirms eksāmena", "Krēmu un desertu gatavošana 2.līmenis", "Krēmu un desertu gatavošana 1.līmenis", "Ķīmija I", "Lietišķās saskarsmes pamatprincipi", "Lietišķās sarakstes nodrošināšana un informācijas aprite", "Latviešu nacionālā un mūsdienu virtuve", "Lietotnes programmēšana", "Lietišķā saskarsme un etiķete", "Lokālo tīklu ierīkošana un uzturēšana", "Latviešu valoda I un Literatūra I", "Maizes gatavošana un cepšana (2.līmenis)", "Matemātika", "Mazumtirdzniecības darbu prakse", "Maizes gatavošana un cepšana (1.līmenis)", "Maizes gatavošanas un cepšanas pamati", "Matemātika I", "Mazumtirdzniecības komercdarbinieku prakse", "Matemātikas speciālās nodaļas", "Mazā uzņēmuma darba vadīšana", "Norēķini ar viesi", "Preču un pakalpojumu iepirkšana tūrisma un viesmīlības uzņēmumā", "Pasākumu apkalpošana", "Pārtikas produktu un izejvielu pirmapstrāde", "Paškontroles sistēmas ēdināšanas uzņēmumā", "Pavāra palīga prakse", "Personas datu aizsardzība", "Pasūtījuma ēdienu gatavošana un noformēšana", "Programmas izstrādes process", "Programmas koda rakstīšana (Kodēšana)", "Prakse komercdarbībā uzņēmumā", "Pavāra prakse", "Pārmiju pārvedu sniega tīrīšanas ierīču un kausēšanas sistēmas apkalpošana", "Prakse tūrisma un viesmīlības uzņēmumā", "Produktu un izstrādājumu uzglabāšana", "Prakse komercdarbībā uzņēmumā", "Profesionālā saziņa svešvalodās angļu val.", "Profesionālās kvalifikācijas eksāmens", "Profesionālā saziņa svešvalodās", "Profesionālā saziņa svešvalodās vācu val.", "Programmēšanas tehnoloģijas", "Programmēšanas tehniķa prakse", "Preču un pakalpojumu iepirkšana", "Preču un pakalpojumu izvēle EIKT infrastruktūras izveidei", "Preču un pakalpojumu iepirkšana tūrisma un viesmīlības uzņēmumā", "Preču un pakalpojumu pārdošana", "Preču uzskaite un dokumentēšana", "Produktu uzskaites un finanšu dokumentēšana", "Personāla vadība", "Preču zinības", "Ražošanas darba organizācija (2.līm.)", "Ražošanas darba organizācija (1.līm.)", "Ritošā sastāva tehniskā stāvokļa automātisko kontrollīdzekļu apkalpošana", "Sabiedrība un cilvēka drošība (1.", "2.līmenis)", "Lietišķa saskarsme un etiķete", "Sesijas eksāmens Matemātikas speciālās nodaļas", "Serveru izvēle un uzstādīšana", "Sociālās zinības un vēsture", "Sistēmu programmēšana", "Sports", "Stunda ar pedagogu - karjeras konsultantu", "Sociālās un pilsoniskās prasmes (2.līmenis)", "Sagatavošanās viesu uzņemšanai", "Šokolādes un cukuroto konditorejas izstrādājumu gatavošana", "Tūrisma un viesmīlības uzņēmuma darbības pamatprocesi", "Tūrisma un viesmīlības pakalpojumu un papildpakalpojumu pārdošana", "Tehnoloģiskās dokumentācijas izstrāde", "Tehnoloģiskās un uzskaites dokumentācijas izveide", "Telpu un vides sagatavošana viesu uzņemšanai", "Tirgus izpēte", "Telpu un darba vietas sagatavošana", "Tirgus un klienti", "Tīmekļa vietņu programmēšana (WEB)", "Uzņēmuma darbības pamatprocesi", "Uzņēmuma mārketinga pasākumu kompleksa izstrāde", "Uzskaites un atskaites dokumentācijas sagatavošana ēdināšanas uzņēmumā", "Uzņēmuma darbības pamatprocesi", "Uzskaites un atskaites dokumentācijas sagatavošana tūrisma un viesmīlības uzņēmumā", "Viesu apkalpošana izmitināšanas un apkalpošanas mītnēs", "Viesu mērķu grupu apkalpošana", "Viesu uzņemšanas dienesta pakalpojumu nodrošināšana", "Viesmīlības pamatprincipi tūrisma un viesmīlības uzņēmumā", "Viesu apkalpošana", "Viesu apkalpošana bārā", "Svešvaloda B1 - vācu valoda", "Vienkāršu algoritmu izstrāde", "Viesu apkalpošana izmitināšanas un apkalpošanas mītnēs", "Valodas", "kultūras izpratne un izpausmes (1.", "2.līmenis)", "Valsts aizsardzības mācība", "Viesmīlības pakalpojumu speciālista prakse", "Viesu apkalpošana izmitināšanas un apkalpošanas mītnēs", "Viesu istabu noformējums specializētiem pasūtījumiem uzņēmumā", "Valodas", "kultūras izpratne un izpausmes (2.līmenis)", "Viesu pasūtījuma izpilde", "Viesu pasūtījuma pieņemšana", "Viesu uzņemšanas dienesta pakalpojumu nodr.", "Zaļās prasmes", "Angļu valoda II", "Personāla vadība", "Budžeta izstrāde", "Trešā svešvaloda (franču val.)", "Reklāmas pakalpojumu organizēšana", "Drošības sistēmu uzstādīšana un uzturēšana", "Piloteksāmens matemātikā", "Piloteksāmens angļu valodā", "Elektronisko sakaru sistēmu lielumu mērījumi", "Dzelzceļa sakaru sistēmas apkalpošana", "Dzelzceļa sakaru sistēmu remonts", "Dzelzceļa automātikas ierīču remonts", "Dzelzceļa automātiskās sistēmas diagnosticēšana", "Dzelzceļa elektroapgādes līniju un ierīču uzturēšana", "Sesijas eksāmens Dzelzceļa automātikas ierīču remonts" }));

        tblGroups1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Students", "Priekšmets", "Atzīme", "Datums"
            }
        ));
        jScrollPane3.setViewportView(tblGroups1);

        btnBack2.setText("Atpakaļ");
        btnBack2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBack2ActionPerformed(evt);
            }
        });

        btnAddStudent2.setText("Pievienot");
        btnAddStudent2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddStudent2ActionPerformed(evt);
            }
        });

        btnClearStudent2.setText("Notīrīt");
        btnClearStudent2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearStudent2ActionPerformed(evt);
            }
        });

        btnUpdateStudent2.setText("Labot");
        btnUpdateStudent2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateStudent2ActionPerformed(evt);
            }
        });

        btnDeleteStudent2.setText("Dzēst");
        btnDeleteStudent2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteStudent2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout GradeDialogLayout = new javax.swing.GroupLayout(GradeDialog.getContentPane());
        GradeDialog.getContentPane().setLayout(GradeDialogLayout);
        GradeDialogLayout.setHorizontalGroup(
            GradeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(GradeDialogLayout.createSequentialGroup()
                .addGap(57, 57, 57)
                .addGroup(GradeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(GradeDialogLayout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addGap(76, 76, 76)
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtGrade, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(GradeDialogLayout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addGroup(GradeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(GradeDialogLayout.createSequentialGroup()
                                .addGap(74, 74, 74)
                                .addComponent(btnBack2, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(36, 36, 36)
                                .addComponent(btnClearStudent2, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(GradeDialogLayout.createSequentialGroup()
                                .addComponent(btnAddStudent2, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(45, 45, 45)
                                .addComponent(btnDeleteStudent2, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(33, 33, 33)
                                .addComponent(btnUpdateStudent2, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel16)
                    .addGroup(GradeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(cmbSubject, 0, 126, Short.MAX_VALUE)
                        .addComponent(cmbStudent, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(40, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, GradeDialogLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(GradeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel19)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 436, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15))
        );
        GradeDialogLayout.setVerticalGroup(
            GradeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(GradeDialogLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel16)
                .addGap(9, 9, 9)
                .addComponent(cmbStudent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(GradeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(GradeDialogLayout.createSequentialGroup()
                        .addGroup(GradeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel18)
                            .addComponent(txtGrade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, GradeDialogLayout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addGap(10, 10, 10)))
                .addComponent(cmbSubject, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(76, 76, 76)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addGroup(GradeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddStudent2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteStudent2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdateStudent2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(GradeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBack2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClearStudent2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(33, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Studentu uzskaites sistēma - Pieteikšanās");

        jLabel1.setText("Parole");

        jLabel2.setText("Lietotājvārds");

        btnLogin.setText("Ielogoties");
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });

        btnRegister.setText("Reģistrēties");
        btnRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(141, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(31, 31, 31)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtUsername, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPassword, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnRegister)
                        .addGap(33, 33, 33)
                        .addComponent(btnLogin)
                        .addGap(16, 16, 16))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLogin)
                    .addComponent(btnRegister))
                .addContainerGap(137, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Apstrādā pieslēgšanos sistēmai (FP-02) un uzstāda piekļuves tiesības atbilstoši lietotāja lomai.
     */
    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lūdzu, aizpildiet visus obligātos laukus.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            User user = new UserDAO().authenticate(username, password);

            if (user == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Nepareizs lietotājvārds vai parole.",
                        "Kļūda",
                        JOptionPane.ERROR_MESSAGE
                );
                txtPassword.setText("");
                return;
            }

            Session.login(user);
            applyRolePermissions();

            txtUsername.setText("");
            txtPassword.setText("");
            this.setVisible(false);

            MainMenuDialog.pack();
            MainMenuDialog.setLocationRelativeTo(null);
            MainMenuDialog.setModal(true);
            MainMenuDialog.setVisible(true);

            this.setLocationRelativeTo(null);
            this.setVisible(true);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Neizdevās izpildīt darbību ar datu bāzi. Mēģiniet vēlreiz.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_btnLoginActionPerformed

    /**
     * Konfigurē statusa joslu un nodrošina, ka visas pogas un ievadlauki ir aktīvi un rediģējami.
     */
    private void applyRolePermissions() {
        User u = Session.getCurrentUser();
        boolean isAdmin = Session.isAdmin();
        String roleLabel = isAdmin ? "Lietvedis (ADMIN)" : "Pasniedzējs (TEACHER)";
        lblLoggedUser.setText("Pieslēdzies kā: " + (u != null ? u.getFullName() : "") + " [" + roleLabel + "]");

        btnStudents.setEnabled(true);
        btnGroups.setEnabled(true);
        btnGrades.setEnabled(true);
        btnReport.setEnabled(true);
        btnHelp.setEnabled(true);
        btnLogout.setEnabled(true);

        btnAddStudent.setEnabled(true);
        btnUpdateStudent.setEnabled(true);
        btnDeleteStudent.setEnabled(true);
        btnClearStudent.setEnabled(true);
        btnBack.setEnabled(true);

        txtFirstName.setEditable(true);
        txtFirstName.setEnabled(true);
        txtLastName.setEditable(true);
        txtLastName.setEnabled(true);
        txtPersonalCode.setEditable(true);
        txtPersonalCode.setEnabled(true);
        txtEmail.setEditable(true);
        txtEmail.setEnabled(true);
        txtPhone.setEditable(true);
        txtPhone.setEnabled(true);
        cmbGroup.setEnabled(true);

        txtGroupName.setEditable(true);
        txtGroupName.setEnabled(true);
        txtGrade.setEditable(true);
        txtGrade.setEnabled(true);
        if (txtGradeDate != null) {
            txtGradeDate.setEditable(true);
            txtGradeDate.setEnabled(true);
        }
        if (txtSearchStudent != null) {
            txtSearchStudent.setEditable(true);
            txtSearchStudent.setEnabled(true);
        }
    }

    /**
     * Atver jauna pasniedzēja reģistrācijas logu (FP-11).
     */
    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterActionPerformed
        txtFullName.setText("");
        txtRegUsername.setText("");
        txtRegPassword.setText("");

        this.setVisible(false);

        RegisterDialog.pack();
        RegisterDialog.setLocationRelativeTo(null);
        RegisterDialog.setModal(true);
        RegisterDialog.setVisible(true);

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }//GEN-LAST:event_btnRegisterActionPerformed

    /**
     * Saglabā jaunu reģistrētu lietotāju (pasniedzēju) datu bāzē (FP-11).
     */
    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        String fullName = txtFullName.getText().trim();
        String username = txtRegUsername.getText().trim();
        String password = new String(txtRegPassword.getPassword());

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    RegisterDialog,
                    "Lūdzu, aizpildiet visus obligātos laukus.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (!Validator.isValidName(fullName)) {
            JOptionPane.showMessageDialog(
                    RegisterDialog,
                    "Vārdam un uzvārdam jāsatur vismaz 2 burti (bez cipariem)!",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (!Validator.isValidUsername(username)) {
            JOptionPane.showMessageDialog(
                    RegisterDialog,
                    "Lietotājvārdam jābūt 3–50 simbolus garam un jāsatur tikai latīņu burti, cipari vai _.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (!Validator.isValidPassword(password)) {
            JOptionPane.showMessageDialog(
                    RegisterDialog,
                    "Parolei jābūt vismaz 6 simboliem!",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            UserDAO userDAO = new UserDAO();
            if (userDAO.usernameExists(username)) {
                JOptionPane.showMessageDialog(
                        RegisterDialog,
                        "Šis lietotājvārds jau ir aizņemts!",
                        "Kļūda",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            boolean registered = userDAO.register(username, password, fullName, "TEACHER");

            if (registered) {
                txtUsername.setText(username);
                txtPassword.setText("");
                JOptionPane.showMessageDialog(
                        RegisterDialog,
                        "Dati veiksmīgi saglabāti.",
                        "Informācija",
                        JOptionPane.INFORMATION_MESSAGE
                );
                RegisterDialog.setVisible(false);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    RegisterDialog,
                    "Neizdevās izpildīt darbību ar datu bāzi. Mēģiniet vēlreiz.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        RegisterDialog.setVisible(false);
    }//GEN-LAST:event_btnCancelActionPerformed

    private void txtFullNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFullNameActionPerformed
    }//GEN-LAST:event_txtFullNameActionPerformed

    private void txtGroupNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtGroupNameActionPerformed
    }//GEN-LAST:event_txtGroupNameActionPerformed

    private void btnStudentsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnStudentsMouseClicked
    }//GEN-LAST:event_btnStudentsMouseClicked

    /**
     * Atver studentu pārvaldības un meklēšanas dialogu (FP-01, FP-04, FP-05, FP-06, FP-10).
     */
    private void btnStudentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStudentsActionPerformed
        loadGroupsIntoStudentCombos();
        clearStudentFields();
        txtSearchStudent.setText("");
        if (cmbFilterGroup.getItemCount() > 0) {
            cmbFilterGroup.setSelectedIndex(0);
        }
        performStudentSearch(false);

        MainMenuDialog.setVisible(false);
        StudentDialog.pack();
        StudentDialog.setLocationRelativeTo(null);
        StudentDialog.setModal(true);
        StudentDialog.setVisible(true);

        clearStudentFields();
        MainMenuDialog.setLocationRelativeTo(null);
        MainMenuDialog.setVisible(true);
    }//GEN-LAST:event_btnStudentsActionPerformed

    private static final String[] DEFAULT_SCHOOL_GROUPS = {
        "K-11", "V-11", "K-21", "V-21", "K-31", "V-31", "K-41", "V-41",
        "KD-11", "KD-21", "KD-31", "KD-41", "P-11", "P-21", "P-31", "P-41",
        "P-41_M", "PV-11", "Vsm-11", "PR-11", "PR-21", "PR-31", "PR-32",
        "ATS-31", "PR-41", "PR-42", "ATS-41", "ET-11", "AE-21", "AE-31",
        "AE-41", "DT-11", "DT-21", "DT-31", "DT-41", "ET-12", "ET-21",
        "ET-31", "ET-41", "FR-11", "FR-21", "FR-31", "LD-11", "LD-21",
        "LD-31", "LD-41", "LDV-11", "TS-11", "TS-21", "TS-31", "KS-41",
        "TS-41", "ESV-11"
    };

    private static final String[] DEFAULT_SCHOOL_TEACHERS = {
        "Nonna Akopova", "Jeļena Aleksejeva", "Ilona Antoneviča", "Inta Baika",
        "Inna Barkova", "Olga Bazarova", "Svetlana Beļakova", "Anda Bernāne",
        "Dmitrijs Bogdanovs", "Gabriela Cimmermane", "Žaneta Dalecka", "Anželika Dorofejeva",
        "Irēna Dortiņa", "Andrejs Dortiņš", "Ilze Drozdovska", "Mihails Fjodorovs",
        "Olga Gaujeniete", "Aleksejs Gorbuls", "Anna Gurtā", "Jeļena Ivanova",
        "Olga Iznova", "Mihails Jenbajevs", "Jeļena Jurtajeva", "Inita Karabekova",
        "Oksana Kartašova", "Daniels Kligulis", "Anastasija Kočmarjova", "Ilona Kokina",
        "Alfrēds Kokins", "Ēdvins Kovaļovs", "Igors Kovaļovs", "Alla Kozočkina",
        "Kristīne Krukovska", "Jeļena Krupska", "Jevģenijs Krupskis", "Ināra Kuharonoka",
        "Tatjana Ļebedeva", "Kirils Ļebedevs", "Dina Ļecka", "Ineta Litauniece",
        "Jūlija Makņa", "Anželika Mihailova", "Gaļina Minčenko", "Jevgeņijs Novajonoks",
        "Ludmila Ņeverova", "Jegors Ņikitins", "Diāna Ogorelova", "Sanita Onckule",
        "Jeļena Oreha", "Jūlija Orliņa", "Anna Ozerska", "Natālija Panteviča",
        "Sandris Pārpucs", "Angelika Paškeviča", "Deniss Poliščuks", "Aleksandra Pomitkina",
        "Oļegs Porietis", "Jekaterina Ribakova", "Ivans Rudzis", "Aleksejs Samohvalovs",
        "Jeļena Sardiko", "Aleksandrs Settarovs", "Dmitrijs Sirotko", "Ērika Skladova",
        "Rita Stepanova", "Iveta Suseja", "Grigorijs Suhovilo", "Žanna Špoģe",
        "Inna Titova", "Inese Šulina", "Maija Vasiļevska", "Elita Vanaģele",
        "Andrejs Vlasenko", "Jurijs Vībe", "Anna Vološina", "Oksana Zuboviča",
        "Nataļja Žemčugova", "Jūlija Žerebkova"
    };

    /**
     * Ielādē grupu sarakstu studenta formas un meklēšanas filtra nolaižamajos sarakstos.
     */
    private void loadGroupsIntoStudentCombos() {
        try {
            GroupDAO groupDAO = new GroupDAO();
            loadedGroups = groupDAO.findAll();

            java.util.LinkedHashSet<String> groupNames = new java.util.LinkedHashSet<>();
            for (StudentGroup g : loadedGroups) {
                groupNames.add(g.getName());
            }
            for (String defGroup : DEFAULT_SCHOOL_GROUPS) {
                groupNames.add(defGroup);
            }

            cmbGroup.removeAllItems();
            for (String name : groupNames) {
                cmbGroup.addItem(name);
            }

            filterGroups = new ArrayList<>(loadedGroups);
            cmbFilterGroup.removeAllItems();
            cmbFilterGroup.addItem("Visas");
            for (StudentGroup g : filterGroups) {
                cmbFilterGroup.addItem(g.getName());
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Neizdevās izpildīt darbību ar datu bāzi. Mēģiniet vēlreiz.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Atlasa un filtrē studentus no datu bāzes atbilstoši meklēšanas tekstam un grupai (FP-06).
     *
     * @param notifyIfEmpty vai rādīt ziņojumu «Nekas nav atrasts.», ja saraksts ir tukšs
     */
    private void performStudentSearch(boolean notifyIfEmpty) {
        try {
            String searchText = txtSearchStudent.getText().trim();
            Integer groupId = null;
            int filterIdx = cmbFilterGroup.getSelectedIndex();
            if (filterIdx > 0 && filterIdx - 1 < filterGroups.size()) {
                groupId = filterGroups.get(filterIdx - 1).getId();
            }

            List<Student> students = new StudentDAO().search(searchText, groupId, null);
            DefaultTableModel model = (DefaultTableModel) tblStudents.getModel();
            model.setRowCount(0);

            for (Student s : students) {
                model.addRow(new Object[]{
                    s.getId(),
                    s.getFirstName(),
                    s.getLastName(),
                    s.getPersonalCode(),
                    s.getEmail(),
                    s.getPhone(),
                    s.getGroupName() != null ? s.getGroupName() : "-"
                });
            }

            if (notifyIfEmpty && students.isEmpty()) {
                JOptionPane.showMessageDialog(
                        StudentDialog,
                        "Nekas nav atrasts.",
                        "Informācija",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    StudentDialog,
                    "Neizdevās izpildīt darbību ar datu bāzi. Mēģiniet vēlreiz.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Aizpilda studenta ievadlaukus, kad tabulā tiek izvēlēta rinda.
     */
    private void populateStudentFieldsFromSelectedRow() {
        int viewRow = tblStudents.getSelectedRow();
        if (viewRow == -1) {
            return;
        }
        int row = tblStudents.convertRowIndexToModel(viewRow);
        DefaultTableModel m = (DefaultTableModel) tblStudents.getModel();
        txtFirstName.setText(valueOrEmpty(m.getValueAt(row, 1)));
        txtLastName.setText(valueOrEmpty(m.getValueAt(row, 2)));
        txtPersonalCode.setText(valueOrEmpty(m.getValueAt(row, 3)));
        txtEmail.setText(valueOrEmpty(m.getValueAt(row, 4)));
        txtPhone.setText(valueOrEmpty(m.getValueAt(row, 5)));
        String groupName = valueOrEmpty(m.getValueAt(row, 6));
        cmbGroup.setSelectedItem(groupName);
    }

    private String valueOrEmpty(Object val) {
        return val == null ? "" : val.toString();
    }

    /**
     * Atver grupu pārvaldības logu (FP-03, FP-04, FP-05, FP-11).
     */
    private void btnGroupsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGroupsActionPerformed
        loadTeachersAndGroupsTable();
        clearGroupFields();

        MainMenuDialog.setVisible(false);
        GroupDialog.pack();
        GroupDialog.setLocationRelativeTo(null);
        GroupDialog.setModal(true);
        GroupDialog.setVisible(true);

        clearGroupFields();
        MainMenuDialog.setLocationRelativeTo(null);
        MainMenuDialog.setVisible(true);
    }//GEN-LAST:event_btnGroupsActionPerformed

    /**
     * Ielādē pasniedzējus un atjaunina grupu tabulu tblGroups no datu bāzes.
     */
    private void loadTeachersAndGroupsTable() {
        try {
            loadedTeachers = new UserDAO().findByRole("TEACHER");
            java.util.LinkedHashSet<String> teacherNames = new java.util.LinkedHashSet<>();
            for (User t : loadedTeachers) {
                teacherNames.add(t.getFullName());
            }
            for (String defTeacher : DEFAULT_SCHOOL_TEACHERS) {
                teacherNames.add(defTeacher);
            }

            cmbTeacher.removeAllItems();
            for (String name : teacherNames) {
                cmbTeacher.addItem(name);
            }

            List<StudentGroup> groups = new GroupDAO().findAll();
            DefaultTableModel model = (DefaultTableModel) tblGroups.getModel();
            model.setRowCount(0);
            for (StudentGroup g : groups) {
                model.addRow(new Object[]{
                    g.getId(),
                    g.getName(),
                    g.getCourse(),
                    g.getTeacherName() != null ? g.getTeacherName() : "-"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    GroupDialog,
                    "Neizdevās izpildīt darbību ar datu bāzi. Mēģiniet vēlreiz.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Aizpilda grupas laukus, kad tabulā tblGroups tiek izvēlēta rinda.
     */
    private void populateGroupFieldsFromSelectedRow() {
        int viewRow = tblGroups.getSelectedRow();
        if (viewRow == -1) {
            return;
        }
        int row = tblGroups.convertRowIndexToModel(viewRow);
        DefaultTableModel m = (DefaultTableModel) tblGroups.getModel();
        txtGroupName.setText(valueOrEmpty(m.getValueAt(row, 1)));
        cmbCourse.setSelectedItem(valueOrEmpty(m.getValueAt(row, 2)));
        String teacherName = valueOrEmpty(m.getValueAt(row, 3));
        cmbTeacher.setSelectedItem(teacherName);
    }

    private void btnGroupsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnGroupsMouseClicked
    }//GEN-LAST:event_btnGroupsMouseClicked

    private void btnGradesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnGradesMouseClicked
    }//GEN-LAST:event_btnGradesMouseClicked

    /**
     * Atver sekmju pārvaldības dialogu (FP-07, FP-08).
     */
    private void openGradeDialog() {
        loadGradeDialogData();
        clearGradeFields();

        MainMenuDialog.setVisible(false);
        GradeDialog.pack();
        GradeDialog.setLocationRelativeTo(null);
        GradeDialog.setModal(true);
        GradeDialog.setVisible(true);

        clearGradeFields();
        MainMenuDialog.setLocationRelativeTo(null);
        MainMenuDialog.setVisible(true);
    }

    /**
     * Ielādē studentu sarakstu un atzīmju tabulu sekmju dialogā.
     */
    private void loadGradeDialogData() {
        try {
            StudentDAO studentDAO = new StudentDAO();
            GradeDAO gradeDAO = new GradeDAO();
            loadedGradeStudents = studentDAO.findAll();
            List<Grade> grades = gradeDAO.findAll();

            cmbStudent.removeAllItems();
            for (Student s : loadedGradeStudents) {
                String grp = s.getGroupName() != null ? " (" + s.getGroupName() + ")" : "";
                cmbStudent.addItem(s.getFullName() + grp);
            }

            DefaultTableModel model = (DefaultTableModel) tblGroups1.getModel();
            model.setRowCount(0);
            for (Grade g : grades) {
                model.addRow(new Object[]{
                    g.getId(),
                    g.getStudentName(),
                    g.getSubject(),
                    g.getGrade(),
                    g.getGradeDate()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    GradeDialog,
                    "Neizdevās izpildīt darbību ar datu bāzi. Mēģiniet vēlreiz.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Aizpilda sekmes laukus, kad tabulā tblGroups1 tiek izvēlēta rinda.
     */
    private void populateGradeFieldsFromSelectedRow() {
        int viewRow = tblGroups1.getSelectedRow();
        if (viewRow == -1) {
            return;
        }
        int row = tblGroups1.convertRowIndexToModel(viewRow);
        DefaultTableModel m = (DefaultTableModel) tblGroups1.getModel();
        String studentName = valueOrEmpty(m.getValueAt(row, 1));
        for (int i = 0; i < loadedGradeStudents.size(); i++) {
            if (loadedGradeStudents.get(i).getFullName().equals(studentName)) {
                cmbStudent.setSelectedIndex(i);
                break;
            }
        }
        cmbSubject.setSelectedItem(valueOrEmpty(m.getValueAt(row, 2)));
        txtGrade.setText(valueOrEmpty(m.getValueAt(row, 3)));
        if (m.getColumnCount() > 4) {
            txtGradeDate.setText(valueOrEmpty(m.getValueAt(row, 4)));
        }
    }

    /**
     * Atver sekmju atskaites dialogu (FP-09).
     */
    private void openReportDialog() {
        try {
            GroupDAO groupDAO = new GroupDAO();
            reportGroups = groupDAO.findAll();

            cmbReportGroup.removeAllItems();
            for (StudentGroup g : reportGroups) {
                cmbReportGroup.addItem(g.getName());
            }

            populateReportStudentsCombo();
            refreshReportTable();

            MainMenuDialog.setVisible(false);
            ReportDialog.setLocationRelativeTo(null);
            ReportDialog.setModal(true);
            ReportDialog.setVisible(true);

            MainMenuDialog.setLocationRelativeTo(null);
            MainMenuDialog.setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    MainMenuDialog,
                    "Neizdevās izpildīt darbību ar datu bāzi. Mēģiniet vēlreiz.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Aizpilda studentu izvēlni atskaites logā atbilstoši izvēlētajai grupai.
     */
    private void populateReportStudentsCombo() {
        cmbReportStudent.removeAllItems();
        cmbReportStudent.addItem("Visi grupas studenti");
        int grpIdx = cmbReportGroup.getSelectedIndex();
        if (grpIdx < 0 || grpIdx >= reportGroups.size()) {
            reportStudents = new ArrayList<>();
            return;
        }
        int groupId = reportGroups.get(grpIdx).getId();
        try {
            reportStudents = new StudentDAO().search(null, groupId);
            for (Student s : reportStudents) {
                cmbReportStudent.addItem(s.getFullName());
            }
        } catch (SQLException ex) {
            reportStudents = new ArrayList<>();
        }
    }

    /**
     * Aprēķina un attēlo studentu un grupas vidējās atzīmes sekmju atskaites tabulā (FP-09).
     */
    private void refreshReportTable() {
        DefaultTableModel model = (DefaultTableModel) tblReport.getModel();
        model.setRowCount(0);

        int grpIdx = cmbReportGroup.getSelectedIndex();
        if (grpIdx < 0 || grpIdx >= reportGroups.size()) {
            lblGroupAverage.setText("Grupas vidējā atzīme: -");
            return;
        }

        StudentGroup group = reportGroups.get(grpIdx);
        int stdIdx = cmbReportStudent.getSelectedIndex();

        try {
            GradeDAO gradeDAO = new GradeDAO();
            List<Student> targetStudents = new ArrayList<>();
            if (stdIdx > 0 && stdIdx - 1 < reportStudents.size()) {
                targetStudents.add(reportStudents.get(stdIdx - 1));
            } else {
                targetStudents.addAll(reportStudents);
            }

            for (Student s : targetStudents) {
                int count = gradeDAO.countForStudent(s.getId());
                double avg = gradeDAO.averageForStudent(s.getId());
                String avgStr = count > 0 ? String.format(Locale.US, "%.2f", avg) : "-";
                model.addRow(new Object[]{
                    s.getFullName(),
                    s.getPersonalCode(),
                    group.getName(),
                    count,
                    avgStr
                });
            }

            double groupAvg = gradeDAO.averageForGroup(group.getId());
            String grpAvgStr = groupAvg > 0 ? String.format(Locale.US, "%.2f", groupAvg) : "-";
            lblGroupAverage.setText("Grupas (" + group.getName() + ") vidējā atzīme: " + grpAvgStr);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    ReportDialog,
                    "Neizdevās izpildīt darbību ar datu bāzi. Mēģiniet vēlreiz.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Izdrukā sekmju atskaites tabulu (FP-09).
     */
    private void printReportTable() {
        try {
            MessageFormat header = new MessageFormat("Sekmju atskaite");
            MessageFormat footer = new MessageFormat(lblGroupAverage.getText() + " | Lapa {0}");
            tblReport.print(JTable.PrintMode.FIT_WIDTH, header, footer);
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(
                    ReportDialog,
                    "Neizdevās izdrukāt atskaiti: " + ex.getMessage(),
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void btnLogoutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnLogoutMouseClicked
    }//GEN-LAST:event_btnLogoutMouseClicked

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        StudentDialog.setVisible(false);
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnBack1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBack1ActionPerformed
        GroupDialog.setVisible(false);
    }//GEN-LAST:event_btnBack1ActionPerformed

    private void btnBack2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBack2ActionPerformed
        GradeDialog.setVisible(false);
    }//GEN-LAST:event_btnBack2ActionPerformed

    /**
     * Iziet no pašreizējās lietotāja sesijas ar apstiprinājuma dialogu (showConfirmDialog).
     */
    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        int confirm = JOptionPane.showConfirmDialog(
                MainMenuDialog,
                "Vai tiešām aizvērt programmu?",
                "Apstiprinājums",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        Session.logout();
        MainMenuDialog.setVisible(false);
        txtUsername.setText("");
        txtPassword.setText("");
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnClearStudentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearStudentActionPerformed
        clearStudentFields();
    }//GEN-LAST:event_btnClearStudentActionPerformed

    private void clearStudentFields() {
        txtFirstName.setText("");
        txtLastName.setText("");
        txtPersonalCode.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        if (cmbGroup.getItemCount() > 0) {
            cmbGroup.setSelectedIndex(0);
        }
        tblStudents.clearSelection();
    }

    /**
     * Pievieno jaunu studentu datu bāzei (FP-01).
     */
    private void btnAddStudentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddStudentActionPerformed
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String personalCode = txtPersonalCode.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        if (personalCode.matches("\\d{11}")) {
            personalCode = personalCode.substring(0, 6) + "-" + personalCode.substring(6);
            txtPersonalCode.setText(personalCode);
        }

        if (!Validator.isNotEmpty(firstName)
                || !Validator.isNotEmpty(lastName)
                || !Validator.isNotEmpty(personalCode)) {
            JOptionPane.showMessageDialog(
                    StudentDialog,
                    "Lūdzu, aizpildiet obligātos laukus (Vārds, Uzvārds, Personas kods)!",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (personalCode.length() > 20) {
            personalCode = personalCode.substring(0, 20);
        }

        try {
            StudentDAO dao = new StudentDAO();

            if (dao.personalCodeExists(personalCode)) {
                JOptionPane.showMessageDialog(
                        StudentDialog,
                        "Students ar šādu personas kodu jau eksistē!",
                        "Kļūda",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Object selectedGroupObj = cmbGroup.getSelectedItem();
            String selectedGroupName = selectedGroupObj != null ? selectedGroupObj.toString().trim() : null;
            Integer groupId = new GroupDAO().findOrCreateByName(selectedGroupName);

            Student student = new Student();
            student.setFirstName(firstName);
            student.setLastName(lastName);
            student.setPersonalCode(personalCode);
            student.setEmail(email);
            student.setPhone(phone);
            student.setGroupId(groupId);

            if (dao.add(student)) {
                loadGroupsIntoStudentCombos();
                txtSearchStudent.setText("");
                if (cmbFilterGroup.getItemCount() > 0) {
                    cmbFilterGroup.setSelectedIndex(0);
                }
                performStudentSearch(false);
                clearStudentFields();
                JOptionPane.showMessageDialog(
                        StudentDialog,
                        "Dati veiksmīgi saglabāti.",
                        "Informācija",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    StudentDialog,
                    "Neizdevās izpildīt darbību ar datu bāzi. Mēģiniet vēlreiz.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_btnAddStudentActionPerformed

    /**
     * Atjaunina izvēlētā studenta datus datu bāzē (FP-04).
     */
    private void btnUpdateStudentActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = tblStudents.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    StudentDialog,
                    "Vispirms izvēlies studentu tabulā!",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String personalCode = txtPersonalCode.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        if (personalCode.matches("\\d{11}")) {
            personalCode = personalCode.substring(0, 6) + "-" + personalCode.substring(6);
            txtPersonalCode.setText(personalCode);
        }

        if (!Validator.isNotEmpty(firstName)
                || !Validator.isNotEmpty(lastName)
                || !Validator.isNotEmpty(personalCode)) {
            JOptionPane.showMessageDialog(
                    StudentDialog,
                    "Lūdzu, aizpildiet obligātos laukus (Vārds, Uzvārds, Personas kods)!",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (personalCode.length() > 20) {
            personalCode = personalCode.substring(0, 20);
        }

        try {
            int modelRow = tblStudents.convertRowIndexToModel(selectedRow);
            int studentId = Integer.parseInt(
                    tblStudents.getModel().getValueAt(modelRow, 0).toString()
            );

            StudentDAO dao = new StudentDAO();
            if (dao.personalCodeExistsForOther(personalCode, studentId)) {
                JOptionPane.showMessageDialog(
                        StudentDialog,
                        "Students ar šādu personas kodu jau eksistē!",
                        "Kļūda",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Object selectedGroupObj = cmbGroup.getSelectedItem();
            String selectedGroupName = selectedGroupObj != null ? selectedGroupObj.toString().trim() : null;
            Integer groupId = new GroupDAO().findOrCreateByName(selectedGroupName);

            Student student = new Student(
                    studentId, firstName, lastName, personalCode, email, phone, groupId
            );

            if (dao.update(student)) {
                loadGroupsIntoStudentCombos();
                performStudentSearch(false);
                clearStudentFields();
                JOptionPane.showMessageDialog(
                        StudentDialog,
                        "Dati veiksmīgi saglabāti.",
                        "Informācija",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    StudentDialog,
                    "Neizdevās izpildīt darbību ar datu bāzi. Mēģiniet vēlreiz.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Dzēš izvēlēto studentu un viņa sekmes pēc lietotāja apstiprinājuma (FP-05).
     */
    private void btnDeleteStudentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteStudentActionPerformed
        int selectedRow = tblStudents.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    StudentDialog,
                    "Vispirms izvēlies studentu tabulā!",
                    "Brīdinājums",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                StudentDialog,
                "Vai tiešām dzēst izvēlēto ierakstu?",
                "Apstiprināt dzēšanu",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int modelRow = tblStudents.convertRowIndexToModel(selectedRow);
            int studentId = Integer.parseInt(
                    tblStudents.getModel().getValueAt(modelRow, 0).toString()
            );

            StudentDAO dao = new StudentDAO();
            if (dao.delete(studentId)) {
                performStudentSearch(false);
                clearStudentFields();
                JOptionPane.showMessageDialog(
                        StudentDialog,
                        "Dati veiksmīgi saglabāti.",
                        "Informācija",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    StudentDialog,
                    "Neizdevās izpildīt darbību ar datu bāzi. Mēģiniet vēlreiz.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_btnDeleteStudentActionPerformed

    private void btnAddStudentMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAddStudentMouseClicked
    }//GEN-LAST:event_btnAddStudentMouseClicked

    /**
     * Dzēš izvēlēto grupu, ja tajā nav studentu (FP-05).
     */
    private void btnDeleteStudent1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteStudent1ActionPerformed
        int row = tblGroups.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(
                    GroupDialog,
                    "Vispirms izvēlies grupu tabulā!",
                    "Brīdinājums",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                GroupDialog,
                "Vai tiešām dzēst izvēlēto ierakstu?",
                "Grupas dzēšana",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int modelRow = tblGroups.convertRowIndexToModel(row);
            int groupId = Integer.parseInt(
                    tblGroups.getModel().getValueAt(modelRow, 0).toString()
            );

            StudentDAO studentDAO = new StudentDAO();
            if (studentDAO.countByGroup(groupId) > 0) {
                JOptionPane.showMessageDialog(
                        GroupDialog,
                        "Grupu nevar dzēst, jo tajā ir studenti.",
                        "Brīdinājums",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            GroupDAO dao = new GroupDAO();
            if (dao.delete(groupId)) {
                loadTeachersAndGroupsTable();
                clearGroupFields();
                JOptionPane.showMessageDialog(
                        GroupDialog,
                        "Dati veiksmīgi saglabāti.",
                        "Informācija",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    GroupDialog,
                    "Neizdevās izpildīt darbību ar datu bāzi. Mēģiniet vēlreiz.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_btnDeleteStudent1ActionPerformed

    /**
     * Pievieno jaunu grupu un piesaista tai izvēlēto pasniedzēju (FP-03, FP-11).
     */
    private void btnAddStudent1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddStudent1ActionPerformed
        String groupName = txtGroupName.getText().trim();

        if (!Validator.isNotEmpty(groupName)) {
            JOptionPane.showMessageDialog(
                    GroupDialog,
                    "Lūdzu, aizpildiet visus obligātos laukus.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            GroupDAO dao = new GroupDAO();
            if (dao.nameExists(groupName)) {
                JOptionPane.showMessageDialog(
                        GroupDialog,
                        "Grupa ar šādu nosaukumu jau eksistē!",
                        "Kļūda",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Object selectedTeacherObj = cmbTeacher.getSelectedItem();
            String selectedTeacherName = selectedTeacherObj != null ? selectedTeacherObj.toString().trim() : null;
            Integer teacherId = new UserDAO().findOrCreateTeacherByFullName(selectedTeacherName);

            StudentGroup group = new StudentGroup();
            group.setName(groupName);
            group.setCourse(Integer.parseInt(cmbCourse.getSelectedItem().toString()));
            group.setTeacherId(teacherId);

            if (dao.add(group)) {
                loadTeachersAndGroupsTable();
                clearGroupFields();
                JOptionPane.showMessageDialog(
                        GroupDialog,
                        "Dati veiksmīgi saglabāti.",
                        "Informācija",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    GroupDialog,
                    "Neizdevās izpildīt darbību ar datu bāzi. Mēģiniet vēlreiz.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_btnAddStudent1ActionPerformed

    /**
     * Labo izvēlētās grupas datus un pasniedzēju (FP-04, FP-11).
     */
    private void btnUpdateStudent1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateStudent1ActionPerformed
        int row = tblGroups.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(
                    GroupDialog,
                    "Vispirms izvēlies grupu tabulā!",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String groupName = txtGroupName.getText().trim();
        if (!Validator.isNotEmpty(groupName)) {
            JOptionPane.showMessageDialog(
                    GroupDialog,
                    "Lūdzu, aizpildiet visus obligātos laukus.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            int modelRow = tblGroups.convertRowIndexToModel(row);
            int groupId = Integer.parseInt(
                    tblGroups.getModel().getValueAt(modelRow, 0).toString()
            );

            GroupDAO dao = new GroupDAO();
            if (dao.nameExistsForOther(groupName, groupId)) {
                JOptionPane.showMessageDialog(
                        GroupDialog,
                        "Grupa ar šādu nosaukumu jau eksistē!",
                        "Kļūda",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Object selectedTeacherObj = cmbTeacher.getSelectedItem();
            String selectedTeacherName = selectedTeacherObj != null ? selectedTeacherObj.toString().trim() : null;
            Integer teacherId = new UserDAO().findOrCreateTeacherByFullName(selectedTeacherName);

            StudentGroup group = new StudentGroup(
                    groupId,
                    groupName,
                    Integer.parseInt(cmbCourse.getSelectedItem().toString()),
                    teacherId
            );

            if (dao.update(group)) {
                loadTeachersAndGroupsTable();
                clearGroupFields();
                JOptionPane.showMessageDialog(
                        GroupDialog,
                        "Dati veiksmīgi saglabāti.",
                        "Informācija",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    GroupDialog,
                    "Neizdevās izpildīt darbību ar datu bāzi. Mēģiniet vēlreiz.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_btnUpdateStudent1ActionPerformed

    private void cmbTeacherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTeacherActionPerformed
    }//GEN-LAST:event_cmbTeacherActionPerformed

    private void btnClearStudent1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearStudent1ActionPerformed
        clearGroupFields();
    }//GEN-LAST:event_btnClearStudent1ActionPerformed

    private void clearGroupFields() {
        txtGroupName.setText("");
        if (cmbTeacher.getItemCount() > 0) {
            cmbTeacher.setSelectedIndex(0);
        }
        if (cmbCourse.getItemCount() > 0) {
            cmbCourse.setSelectedIndex(0);
        }
        tblGroups.clearSelection();
    }

    /**
     * Pievieno jaunu sekmi datu bāzei (FP-07).
     */
    private void btnAddStudent2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddStudent2ActionPerformed
        int studentIdx = cmbStudent.getSelectedIndex();
        if (studentIdx < 0 || studentIdx >= loadedGradeStudents.size()) {
            JOptionPane.showMessageDialog(
                    GradeDialog,
                    "Lūdzu, aizpildiet visus obligātos laukus.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String gradeText = txtGrade.getText().trim();
        String dateText = txtGradeDate.getText().trim();
        Object subjObj = cmbSubject.getSelectedItem();
        String subject = subjObj != null ? subjObj.toString().trim() : "";

        if (gradeText.isEmpty() || dateText.isEmpty() || !Validator.isValidSubject(subject)) {
            JOptionPane.showMessageDialog(
                    GradeDialog,
                    "Lūdzu, aizpildiet visus obligātos laukus.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int gradeValue;
        try {
            gradeValue = Integer.parseInt(gradeText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    GradeDialog,
                    "Atzīmei jābūt veselam skaitlim no 1 līdz 10.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (!Validator.isValidGrade(gradeValue)) {
            JOptionPane.showMessageDialog(
                    GradeDialog,
                    "Atzīmei jābūt veselam skaitlim no 1 līdz 10.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        LocalDate gradeDate;
        try {
            gradeDate = LocalDate.parse(dateText);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(
                    GradeDialog,
                    "Datumam jābūt formātā GGGG-MM-DD (piemēram, 2026-10-25)!",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            Student selectedStudent = loadedGradeStudents.get(studentIdx);
            Integer teacherId = Session.getCurrentUser() != null
                    ? Session.getCurrentUser().getId()
                    : null;

            Grade g = new Grade(0, selectedStudent.getId(), subject, gradeValue, gradeDate, teacherId);
            if (new GradeDAO().add(g)) {
                loadGradeDialogData();
                clearGradeFields();
                JOptionPane.showMessageDialog(
                        GradeDialog,
                        "Dati veiksmīgi saglabāti.",
                        "Informācija",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    GradeDialog,
                    "Neizdevās izpildīt darbību ar datu bāzi. Mēģiniet vēlreiz.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_btnAddStudent2ActionPerformed

    /**
     * Labo izvēlēto sekmi datu bāzē (FP-08).
     */
    private void btnUpdateStudent2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateStudent2ActionPerformed
        int row = tblGroups1.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(
                    GradeDialog,
                    "Vispirms izvēlies atzīmi tabulā!",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int studentIdx = cmbStudent.getSelectedIndex();
        if (studentIdx < 0 || studentIdx >= loadedGradeStudents.size()) {
            JOptionPane.showMessageDialog(
                    GradeDialog,
                    "Lūdzu, aizpildiet visus obligātos laukus.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String gradeText = txtGrade.getText().trim();
        String dateText = txtGradeDate.getText().trim();
        Object subjObj = cmbSubject.getSelectedItem();
        String subject = subjObj != null ? subjObj.toString().trim() : "";

        if (gradeText.isEmpty() || dateText.isEmpty() || !Validator.isValidSubject(subject)) {
            JOptionPane.showMessageDialog(
                    GradeDialog,
                    "Lūdzu, aizpildiet visus obligātos laukus.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int gradeValue;
        try {
            gradeValue = Integer.parseInt(gradeText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    GradeDialog,
                    "Atzīmei jābūt veselam skaitlim no 1 līdz 10.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (!Validator.isValidGrade(gradeValue)) {
            JOptionPane.showMessageDialog(
                    GradeDialog,
                    "Atzīmei jābūt veselam skaitlim no 1 līdz 10.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        LocalDate gradeDate;
        try {
            gradeDate = LocalDate.parse(dateText);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(
                    GradeDialog,
                    "Datumam jābūt formātā GGGG-MM-DD (piemēram, 2026-10-25)!",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            int modelRow = tblGroups1.convertRowIndexToModel(row);
            int gradeId = Integer.parseInt(
                    tblGroups1.getModel().getValueAt(modelRow, 0).toString()
            );
            Student selectedStudent = loadedGradeStudents.get(studentIdx);
            Integer teacherId = Session.getCurrentUser() != null
                    ? Session.getCurrentUser().getId()
                    : null;

            Grade g = new Grade(gradeId, selectedStudent.getId(), subject, gradeValue, gradeDate, teacherId);
            if (new GradeDAO().update(g)) {
                loadGradeDialogData();
                clearGradeFields();
                JOptionPane.showMessageDialog(
                        GradeDialog,
                        "Dati veiksmīgi saglabāti.",
                        "Informācija",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    GradeDialog,
                    "Neizdevās izpildīt darbību ar datu bāzi. Mēģiniet vēlreiz.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_btnUpdateStudent2ActionPerformed

    /**
     * Dzēš izvēlēto sekmi no datu bāzes pēc apstiprinājuma (FP-08).
     */
    private void btnDeleteStudent2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteStudent2ActionPerformed
        int row = tblGroups1.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(
                    GradeDialog,
                    "Vispirms izvēlies atzīmi tabulā!",
                    "Brīdinājums",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                GradeDialog,
                "Vai tiešām dzēst izvēlēto ierakstu?",
                "Atzīmes dzēšana",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int modelRow = tblGroups1.convertRowIndexToModel(row);
            int gradeId = Integer.parseInt(
                    tblGroups1.getModel().getValueAt(modelRow, 0).toString()
            );

            if (new GradeDAO().delete(gradeId)) {
                loadGradeDialogData();
                clearGradeFields();
                JOptionPane.showMessageDialog(
                        GradeDialog,
                        "Dati veiksmīgi saglabāti.",
                        "Informācija",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    GradeDialog,
                    "Neizdevās izpildīt darbību ar datu bāzi. Mēģiniet vēlreiz.",
                    "Kļūda",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_btnDeleteStudent2ActionPerformed

    private void btnClearStudent2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearStudent2ActionPerformed
        clearGradeFields();
    }//GEN-LAST:event_btnClearStudent2ActionPerformed

    private void txtFirstNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFirstNameActionPerformed
        // TODO add youring code here:
    }//GEN-LAST:event_txtFirstNameActionPerformed

    private void clearGradeFields() {
        if (cmbStudent.getItemCount() > 0) {
            cmbStudent.setSelectedIndex(0);
        }
        if (cmbSubject.getItemCount() > 0) {
            cmbSubject.setSelectedIndex(0);
        }
        txtGrade.setText("");
        if (txtGradeDate != null) {
            txtGradeDate.setText(LocalDate.now().toString());
        }
        tblGroups1.clearSelection();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog GradeDialog;
    private javax.swing.JDialog GroupDialog;
    private javax.swing.JDialog MainMenuDialog;
    private javax.swing.JDialog RegisterDialog;
    private javax.swing.JDialog StudentDialog;
    private javax.swing.JButton btnAddStudent;
    private javax.swing.JButton btnAddStudent1;
    private javax.swing.JButton btnAddStudent2;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnBack1;
    private javax.swing.JButton btnBack2;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnClearStudent;
    private javax.swing.JButton btnClearStudent1;
    private javax.swing.JButton btnClearStudent2;
    private javax.swing.JButton btnDeleteStudent;
    private javax.swing.JButton btnDeleteStudent1;
    private javax.swing.JButton btnDeleteStudent2;
    private javax.swing.JButton btnGrades;
    private javax.swing.JButton btnGroups;
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnRegister;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnStudents;
    private javax.swing.JButton btnUpdateStudent;
    private javax.swing.JButton btnUpdateStudent1;
    private javax.swing.JButton btnUpdateStudent2;
    private javax.swing.JComboBox<String> cmbCourse;
    private javax.swing.JComboBox<String> cmbGroup;
    private javax.swing.JComboBox<String> cmbStudent;
    private javax.swing.JComboBox<String> cmbSubject;
    private javax.swing.JComboBox<String> cmbTeacher;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JTable tblGroups;
    private javax.swing.JTable tblGroups1;
    private javax.swing.JTable tblStudents;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFirstName;
    private javax.swing.JTextField txtFullName;
    private javax.swing.JTextField txtGrade;
    private javax.swing.JTextField txtGroupName;
    private javax.swing.JTextField txtLastName;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtPersonalCode;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JPasswordField txtRegPassword;
    private javax.swing.JTextField txtRegUsername;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
