package studentuuzskaitesistema.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.imageio.ImageIO;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import studentuuzskaitesistema.dao.UserDAO;
import studentuuzskaitesistema.db.DBConnection;
import studentuuzskaitesistema.model.User;
import studentuuzskaitesistema.util.Session;

/**
 * Palīgklase dokumentācijas diagrammu un lietotāja saskarnes ekrānuzņēmumu
 * automātiskai ģenerēšanai.
 */
public class DocAssetGenerator {

    private static File outDir;

    public static void main(String[] args) throws Exception {
        outDir = new File("build/doc_assets");
        outDir.mkdirs();

        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }

        DBConnection.initialize();

        drawDomainModelDiagram(new File(outDir, "fig_1_1_domain.png"));
        drawErDiagram(new File(outDir, "fig_1_5_er.png"));
        drawStructureDiagram(new File(outDir, "fig_2_1_structure.png"));
        drawModelUmlDiagram(new File(outDir, "fig_2_2_model_uml.png"));
        drawDaoUmlDiagram(new File(outDir, "fig_2_3_dao_uml.png"));

        SwingUtilities.invokeAndWait(() -> {
            try {
                captureGuiScreenshots();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("Visi attēli saglabāti: " + outDir.getAbsolutePath());
        System.exit(0);
    }

    private static void captureGuiScreenshots() throws Exception {
        LoginFrame lf = new LoginFrame();
        lf.pack();
        lf.setSize(440, 270);
        lf.setLocationRelativeTo(null);
        lf.setVisible(true);

        JTextField txtUsername = getField(lf, "txtUsername", JTextField.class);
        JPasswordField txtPassword = getField(lf, "txtPassword", JPasswordField.class);
        JDialog registerDialog = getField(lf, "RegisterDialog", JDialog.class);
        JDialog mainMenuDialog = getField(lf, "MainMenuDialog", JDialog.class);
        JDialog studentDialog = getField(lf, "StudentDialog", JDialog.class);
        JDialog groupDialog = getField(lf, "GroupDialog", JDialog.class);
        JDialog gradeDialog = getField(lf, "GradeDialog", JDialog.class);
        JDialog reportDialog = getField(lf, "ReportDialog", JDialog.class);

        // 1. LoginFrame tukšs un ar ievadītu admin
        txtUsername.setText("admin");
        txtPassword.setText("admin123");
        saveWindowImage(lf, new File(outDir, "gui_login.png"));

        // 2. RegisterDialog
        JTextField txtFullName = getField(lf, "txtFullName", JTextField.class);
        JTextField txtRegUsername = getField(lf, "txtRegUsername", JTextField.class);
        JPasswordField txtRegPassword = getField(lf, "txtRegPassword", JPasswordField.class);
        txtFullName.setText("Ilze Kalniņa");
        txtRegUsername.setText("ikalnina");
        txtRegPassword.setText("parole123");
        registerDialog.pack();
        registerDialog.setSize(420, 290);
        saveDialogImage(registerDialog, new File(outDir, "gui_register.png"));

        // 3. Pieslēgšanās kā ADMIN un MainMenuDialog
        User admin = new UserDAO().authenticate("admin", "admin123");
        Session.login(admin);
        invokePrivate(lf, "applyRolePermissions");
        mainMenuDialog.pack();
        mainMenuDialog.setSize(480, 400);
        saveDialogImage(mainMenuDialog, new File(outDir, "gui_main_admin.png"));

        // 4. StudentDialog (ADMIN skats ar visiem studentiem un izvēlētu 1. rindu)
        invokePrivate(lf, "loadGroupsIntoStudentCombos");
        JTextField txtSearchStudent = getField(lf, "txtSearchStudent", JTextField.class);
        JComboBox<?> cmbFilterGroup = getField(lf, "cmbFilterGroup", JComboBox.class);
        txtSearchStudent.setText("");
        cmbFilterGroup.setSelectedIndex(0);
        invokePrivate(lf, "performStudentSearch", new Class<?>[]{boolean.class}, new Object[]{false});
        JTable tblStudents = getField(lf, "tblStudents", JTable.class);
        if (tblStudents.getRowCount() > 0) {
            tblStudents.setRowSelectionInterval(0, 0);
            invokePrivate(lf, "populateStudentFieldsFromSelectedRow");
        }
        studentDialog.pack();
        studentDialog.setSize(720, 520);
        saveDialogImage(studentDialog, new File(outDir, "gui_students_admin.png"));

        // 5. StudentDialog meklēšanas/filtrēšanas piemērs (T05)
        txtSearchStudent.setText("Anna");
        cmbFilterGroup.setSelectedIndex(0);
        invokePrivate(lf, "performStudentSearch", new Class<?>[]{boolean.class}, new Object[]{false});
        saveDialogImage(studentDialog, new File(outDir, "gui_students_search.png"));

        // Atjaunojam pilnu sarakstu
        txtSearchStudent.setText("");
        invokePrivate(lf, "performStudentSearch", new Class<?>[]{boolean.class}, new Object[]{false});

        // 6. GroupDialog (ADMIN skats)
        invokePrivate(lf, "loadTeachersAndGroupsTable");
        JTable tblGroups = getField(lf, "tblGroups", JTable.class);
        if (tblGroups.getRowCount() > 0) {
            tblGroups.setRowSelectionInterval(0, 0);
            invokePrivate(lf, "populateGroupFieldsFromSelectedRow");
        }
        groupDialog.pack();
        groupDialog.setSize(560, 520);
        saveDialogImage(groupDialog, new File(outDir, "gui_groups.png"));

        // 7. GradeDialog
        invokePrivate(lf, "loadGradeDialogData");
        JTable tblGrades = getField(lf, "tblGroups1", JTable.class);
        if (tblGrades.getRowCount() > 0) {
            tblGrades.setRowSelectionInterval(0, 0);
            invokePrivate(lf, "populateGradeFieldsFromSelectedRow");
        }
        gradeDialog.pack();
        gradeDialog.setSize(580, 540);
        saveDialogImage(gradeDialog, new File(outDir, "gui_grades.png"));

        // 8. ReportDialog (Sekmju atskaite)
        studentuuzskaitesistema.dao.GroupDAO groupDAO = new studentuuzskaitesistema.dao.GroupDAO();
        Field reportGroupsField = LoginFrame.class.getDeclaredField("reportGroups");
        reportGroupsField.setAccessible(true);
        java.util.List<studentuuzskaitesistema.model.StudentGroup> rg = groupDAO.findAll();
        reportGroupsField.set(lf, rg);
        @SuppressWarnings("unchecked")
        JComboBox<String> cmbReportGroup = getField(lf, "cmbReportGroup", JComboBox.class);
        cmbReportGroup.removeAllItems();
        for (studentuuzskaitesistema.model.StudentGroup g : rg) {
            cmbReportGroup.addItem(g.getName());
        }
        invokePrivate(lf, "populateReportStudentsCombo");
        invokePrivate(lf, "refreshReportTable");
        reportDialog.setSize(680, 420);
        saveDialogImage(reportDialog, new File(outDir, "gui_report.png"));

        // 9. TEACHER lomas skats (MainMenuDialog ar pelēkotu pogu "Grupas")
        User teacher = new UserDAO().authenticate("pasniedzejs", "teacher123");
        Session.login(teacher);
        invokePrivate(lf, "applyRolePermissions");
        mainMenuDialog.pack();
        mainMenuDialog.setSize(480, 400);
        saveDialogImage(mainMenuDialog, new File(outDir, "gui_main_teacher.png"));

        // 10. JOptionPane paziņojumu logi (Palīdzība, Kļūda, Apstiprinājums, Informācija)
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
        saveOptionPaneImage(helpText, "Palīdzība (Help)", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, new File(outDir, "gui_help.png"));
        saveOptionPaneImage("Nepareizs lietotājvārds vai parole.", "Kļūda", JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, new File(outDir, "gui_msg_login_err.png"));
        saveOptionPaneImage("Atzīmei jābūt veselam skaitlim no 1 līdz 10.", "Kļūda", JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, new File(outDir, "gui_msg_grade_err.png"));
        saveOptionPaneImage("Grupu nevar dzēst, jo tajā ir studenti.", "Brīdinājums", JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION, new File(outDir, "gui_msg_group_warn.png"));
        saveOptionPaneImage("Dati veiksmīgi saglabāti.", "Informācija", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, new File(outDir, "gui_msg_saved.png"));
        saveOptionPaneImage("Vai tiešām dzēst izvēlēto ierakstu?", "Apstiprināt dzēšanu", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, new File(outDir, "gui_msg_confirm_delete.png"));

        lf.dispose();
    }

    private static void saveOptionPaneImage(String msg, String title, int msgType, int optType, File file) throws Exception {
        JOptionPane pane = new JOptionPane(msg, msgType, optType);
        JDialog dialog = pane.createDialog(null, title);
        dialog.setModal(false);
        dialog.pack();
        saveDialogImage(dialog, file);
        dialog.dispose();
    }

    private static void saveDialogImage(JDialog dialog, File file) throws Exception {
        dialog.setModal(false);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        dialog.validate();
        dialog.repaint();
        saveComponentWithTitleBar(dialog.getContentPane(), dialog.getTitle(), file);
        dialog.setVisible(false);
    }

    private static void saveWindowImage(JFrame frame, File file) throws Exception {
        frame.validate();
        frame.repaint();
        saveComponentWithTitleBar(frame.getContentPane(), frame.getTitle(), file);
    }

    private static void saveComponentWithTitleBar(Component content, String title, File file) throws Exception {
        int w = Math.max(content.getWidth(), content.getPreferredSize().width);
        int h = Math.max(content.getHeight(), content.getPreferredSize().height);
        content.setSize(w, h);
        content.doLayout();

        int titleH = 32;
        int border = 2;
        int totalW = w + border * 2;
        int totalH = h + titleH + border * 2;

        BufferedImage img = new BufferedImage(totalW, totalH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        setupGraphics(g2);

        // Loga rāmis un virsrakstjosla
        g2.setColor(new Color(225, 232, 240));
        g2.fillRect(0, 0, totalW, titleH);
        g2.setColor(new Color(70, 95, 130));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(0, 0, totalW - 1, totalH - 1);
        g2.drawLine(0, titleH, totalW, titleH);

        g2.setColor(new Color(25, 35, 50));
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString(title != null ? title : "", 12, 21);

        // Aizvēršanas ikona X
        g2.setColor(new Color(90, 100, 115));
        g2.drawString("✕", totalW - 22, 21);

        g2.translate(border, titleH);
        content.printAll(g2);
        g2.dispose();

        ImageIO.write(img, "png", file);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object obj, String name, Class<T> cls) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return (T) f.get(obj);
    }

    private static void invokePrivate(Object obj, String methodName) throws Exception {
        Method m = obj.getClass().getDeclaredMethod(methodName);
        m.setAccessible(true);
        m.invoke(obj);
    }

    private static void invokePrivate(Object obj, String methodName, Class<?>[] types, Object[] args) throws Exception {
        Method m = obj.getClass().getDeclaredMethod(methodName, types);
        m.setAccessible(true);
        m.invoke(obj, args);
    }

    private static void setupGraphics(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    }

    /**
     * 1.1. att. Priekšmetiskās jomas informācijas modelis (4 entītijas).
     */
    private static void drawDomainModelDiagram(File file) throws Exception {
        int w = 960, h = 480;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        setupGraphics(g2);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);

        // Saites
        drawRelationLine(g2, 300, 130, 560, 130, "pārzina (1 : N)");
        drawRelationLine(g2, 710, 210, 710, 290, "ietver (1 : N)");
        drawRelationLine(g2, 300, 355, 560, 355, "saņem (N : 1)");
        drawRelationLine(g2, 170, 210, 170, 290, "ievada (1 : N)");

        drawDomainBox(g2, 40, 50, 260, 160, "Lietotājs",
                new String[]{"lietotājvārds", "paroles jaucējkods", "pilns vārds", "loma (ADMIN / TEACHER)"},
                new Color(232, 241, 250), new Color(45, 95, 145));

        drawDomainBox(g2, 560, 50, 300, 160, "Grupa",
                new String[]{"grupas nosaukums (piem., PR-32)", "kurss (1–4)", "piesaistītais pasniedzējs"},
                new Color(232, 241, 250), new Color(45, 95, 145));

        drawDomainBox(g2, 40, 290, 260, 150, "Sekme",
                new String[]{"mācību priekšmets", "atzīme (1–10)", "datums"},
                new Color(230, 246, 236), new Color(45, 125, 85));

        drawDomainBox(g2, 560, 290, 300, 150, "Students",
                new String[]{"vārds, uzvārds", "personas kods (123456-12345)", "e-pasts, tālrunis", "studiju grupa"},
                new Color(232, 241, 250), new Color(45, 95, 145));

        g2.dispose();
        ImageIO.write(img, "png", file);
    }

    /**
     * 1.5. att. Datu bāzes ER diagramma (4 tabulas: USERS, STUDENT_GROUPS, STUDENTS, GRADES).
     */
    private static void drawErDiagram(File file) throws Exception {
        int w = 980, h = 540;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        setupGraphics(g2);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);

        // Saites starp tabulām
        drawErConnectorH(g2, 350, 130, 580, 130, "1", "N", "teacher_id");
        drawErConnectorV(g2, 745, 215, 745, 295, "1", "N", "group_id");
        drawErConnectorH(g2, 370, 395, 580, 395, "N", "1", "student_id");
        drawErConnectorV(g2, 195, 215, 195, 295, "1", "N", "teacher_id");

        drawErTable(g2, 40, 40, 310, 175, "USERS", new String[]{
            "PK  id : INT (IDENTITY)",
            "UQ  username : VARCHAR(50) NOT NULL",
            "    password_hash : VARCHAR(255) NOT NULL",
            "    full_name : VARCHAR(100) NOT NULL",
            "    role : VARCHAR(20) NOT NULL"
        });

        drawErTable(g2, 580, 40, 330, 175, "STUDENT_GROUPS", new String[]{
            "PK  id : INT (IDENTITY)",
            "UQ  name : VARCHAR(30) NOT NULL",
            "    course : INT NOT NULL",
            "FK  teacher_id : INT -> USERS(id)"
        });

        drawErTable(g2, 40, 295, 330, 195, "GRADES", new String[]{
            "PK  id : INT (IDENTITY)",
            "FK  student_id : INT NOT NULL -> STUDENTS(id)",
            "    subject : VARCHAR(100) NOT NULL",
            "    grade : INT NOT NULL CHECK (1..10)",
            "    grade_date : DATE NOT NULL",
            "FK  teacher_id : INT -> USERS(id)"
        });

        drawErTable(g2, 580, 295, 330, 205, "STUDENTS", new String[]{
            "PK  id : INT (IDENTITY)",
            "    first_name : VARCHAR(50) NOT NULL",
            "    last_name : VARCHAR(50) NOT NULL",
            "UQ  personal_code : VARCHAR(20) NOT NULL",
            "    email : VARCHAR(100)",
            "    phone : VARCHAR(30)",
            "FK  group_id : INT -> STUDENT_GROUPS(id)"
        });

        g2.dispose();
        ImageIO.write(img, "png", file);
    }

    /**
     * 2.1. att. Sistēmas struktūras modelis (4 slāņi).
     */
    private static void drawStructureDiagram(File file) throws Exception {
        int w = 980, h = 600;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        setupGraphics(g2);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);

        // 1. GUI slānis
        drawLayerContainer(g2, 30, 20, 920, 115, "GUI slānis (studentuuzskaitesistema.gui – Java Swing)",
                new Color(238, 242, 252), new Color(65, 85, 165));
        String[] guiBoxes = {
            "StudentuUzskaitesSistema\n(main klase)",
            "LoginFrame\n(JFrame)",
            "MainMenu / Register\n(JDialog)",
            "Student / Group\n(JDialog)",
            "Grade / Report / Help\n(JDialog)"
        };
        for (int i = 0; i < guiBoxes.length; i++) {
            drawSmallNode(g2, 48 + i * 180, 56, 168, 64, guiBoxes[i], new Color(65, 85, 165));
        }

        drawArrowDown(g2, 490, 135, 490, 165, "izsauc un validē datus");

        // 2. DAO un Util slānis
        drawLayerContainer(g2, 30, 165, 920, 115, "DAO un palīgklasu slānis (studentuuzskaitesistema.dao, .model, .util)",
                new Color(234, 247, 238), new Color(45, 130, 85));
        String[] daoBoxes = {
            "UserDAO\n(autentifikācija)",
            "GroupDAO\n(grupu CRUD)",
            "StudentDAO\n(studentu CRUD, meklēšana)",
            "GradeDAO\n(sekmju CRUD, vidējās)",
            "Validator, Session,\nPasswordUtil, Model"
        };
        for (int i = 0; i < daoBoxes.length; i++) {
            drawSmallNode(g2, 48 + i * 180, 201, 168, 64, daoBoxes[i], new Color(45, 130, 85));
        }

        drawArrowDown(g2, 490, 280, 490, 310, "izmanto JDBC savienojumu");

        // 3. JDBC slānis
        drawLayerContainer(g2, 30, 310, 920, 105, "JDBC datu bāzes savienojuma slānis (studentuuzskaitesistema.db)",
                new Color(248, 245, 234), new Color(130, 105, 50));
        drawSmallNode(g2, 180, 344, 620, 56, "DBConnection (getConnection, initialize – Connection, PreparedStatement, ResultSet)",
                new Color(130, 105, 50));

        drawArrowDown(g2, 490, 415, 490, 445, "savienojas ar (jdbc:derby:StudentuUzskaitesDB;create=true)");

        // 4. Apache Derby slānis
        drawLayerContainer(g2, 30, 445, 920, 115, "Apache Derby iegultā datu bāze (Embedded)",
                new Color(238, 242, 252), new Color(65, 85, 165));
        String[] dbTables = {"USERS", "STUDENT_GROUPS", "STUDENTS", "GRADES"};
        for (int i = 0; i < dbTables.length; i++) {
            drawSmallNode(g2, 60 + i * 220, 484, 195, 58, dbTables[i], new Color(65, 85, 165));
        }

        g2.dispose();
        ImageIO.write(img, "png", file);
    }

    /**
     * 2.2. att. Datu modeļa UML klašu diagramma (User, StudentGroup, Student, Grade).
     */
    private static void drawModelUmlDiagram(File file) throws Exception {
        int w = 980, h = 580;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        setupGraphics(g2);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);

        drawErConnectorH(g2, 370, 130, 560, 130, "1", "0..*", "pārzina");
        drawErConnectorV(g2, 740, 240, 740, 300, "1", "0..*", "ietver");
        drawErConnectorH(g2, 370, 420, 560, 420, "0..*", "1", "saņem");
        drawErConnectorV(g2, 200, 240, 200, 300, "1", "0..*", "ievada");

        drawUmlClass(g2, 40, 30, 330, 210, "User",
                new String[]{
                    "- id: int",
                    "- username: String",
                    "- passwordHash: String",
                    "- fullName: String",
                    "- role: String"
                },
                new String[]{
                    "+ isAdmin(): boolean",
                    "+ isTeacher(): boolean",
                    "+ getId() / getUsername(): ...",
                    "+ toString(): String"
                },
                new Color(255, 251, 224));

        drawUmlClass(g2, 560, 30, 360, 210, "StudentGroup",
                new String[]{
                    "- id: int",
                    "- name: String",
                    "- course: int",
                    "- teacherId: Integer",
                    "- teacherName: String"
                },
                new String[]{
                    "+ getId() / getName(): ...",
                    "+ getCourse(): int",
                    "+ getTeacherId(): Integer",
                    "+ toString(): String"
                },
                new Color(255, 251, 224));

        drawUmlClass(g2, 40, 300, 330, 240, "Grade",
                new String[]{
                    "- id: int",
                    "- studentId: int",
                    "- subject: String",
                    "- grade: int",
                    "- gradeDate: LocalDate",
                    "- teacherId: Integer",
                    "- studentName: String"
                },
                new String[]{
                    "+ getId() / getStudentId(): int",
                    "+ getSubject() / getGrade(): ...",
                    "+ getGradeDate(): LocalDate"
                },
                new Color(255, 251, 224));

        drawUmlClass(g2, 560, 300, 360, 240, "Student",
                new String[]{
                    "- id: int",
                    "- firstName: String",
                    "- lastName: String",
                    "- personalCode: String",
                    "- email: String",
                    "- phone: String",
                    "- groupId: Integer",
                    "- groupName: String"
                },
                new String[]{
                    "+ getFullName(): String",
                    "+ getPersonalCode(): String",
                    "+ getGroupId(): Integer"
                },
                new Color(255, 251, 224));

        g2.dispose();
        ImageIO.write(img, "png", file);
    }

    /**
     * 2.3. att. Datu piekļuves (DAO) un palīgklasu UML diagramma.
     */
    private static void drawDaoUmlDiagram(File file) throws Exception {
        int w = 980, h = 640;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        setupGraphics(g2);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);

        // DBConnection augšā
        drawUmlClass(g2, 310, 20, 360, 125, "DBConnection",
                new String[]{"- DB_URL: String"},
                new String[]{"+ getConnection(): Connection", "+ initialize(): void"},
                new Color(245, 242, 232));

        drawArrowDown(g2, 490, 145, 490, 175, "izmanto savienojumam");

        // DAO klases vidū
        drawUmlClass(g2, 30, 180, 445, 195, "UserDAO",
                new String[]{},
                new String[]{
                    "+ authenticate(String, String): User",
                    "+ register(String, String, String, String): boolean",
                    "+ usernameExists(String): boolean",
                    "+ findByRole(String): List<User>",
                    "+ findAll(): List<User>"
                },
                new Color(232, 246, 236));

        drawUmlClass(g2, 505, 180, 445, 195, "GroupDAO",
                new String[]{},
                new String[]{
                    "+ add(StudentGroup): boolean",
                    "+ update(StudentGroup): boolean",
                    "+ delete(int): boolean",
                    "+ findAll(): List<StudentGroup>",
                    "+ findByTeacher(int): List<StudentGroup>"
                },
                new Color(232, 246, 236));

        drawUmlClass(g2, 30, 400, 445, 215, "StudentDAO",
                new String[]{},
                new String[]{
                    "+ add(Student): boolean",
                    "+ update(Student): boolean",
                    "+ delete(int): boolean",
                    "+ findAll() / findByTeacher(int): List<Student>",
                    "+ search(String, Integer, Integer): List<Student>",
                    "+ personalCodeExists(String): boolean",
                    "+ countByGroup(int): int"
                },
                new Color(232, 246, 236));

        drawUmlClass(g2, 505, 400, 445, 215, "GradeDAO",
                new String[]{},
                new String[]{
                    "+ add(Grade): boolean",
                    "+ update(Grade): boolean",
                    "+ delete(int): boolean",
                    "+ findAll() / findByTeacher(int): List<Grade>",
                    "+ findByStudent(int) / findByGroup(int): List<Grade>",
                    "+ averageForStudent(int): double",
                    "+ averageForGroup(int): double"
                },
                new Color(232, 246, 236));

        g2.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void drawDomainBox(Graphics2D g2, int x, int y, int w, int h, String title, String[] attrs, Color bg, Color border) {
        g2.setColor(bg);
        g2.fillRoundRect(x, y, w, h, 16, 16);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(2.2f));
        g2.drawRoundRect(x, y, w, h, 16, 16);
        g2.drawLine(x + 12, y + 38, x + w - 12, y + 38);

        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, x + (w - fm.stringWidth(title)) / 2, y + 26);

        g2.setColor(new Color(30, 30, 30));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        for (int i = 0; i < attrs.length; i++) {
            g2.drawString("• " + attrs[i], x + 18, y + 64 + i * 24);
        }
    }

    private static void drawRelationLine(Graphics2D g2, int x1, int y1, int x2, int y2, String label) {
        g2.setColor(new Color(80, 80, 80));
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawLine(x1, y1, x2, y2);
        int mx = (x1 + x2) / 2;
        int my = (y1 + y2) / 2;
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        int lw = fm.stringWidth(label) + 12;
        g2.setColor(Color.WHITE);
        g2.fillRect(mx - lw / 2, my - 12, lw, 20);
        g2.setColor(new Color(50, 50, 50));
        g2.drawString(label, mx - fm.stringWidth(label) / 2, my + 3);
    }

    private static void drawErTable(Graphics2D g2, int x, int y, int w, int h, String tableName, String[] cols) {
        int headerH = 34;
        g2.setColor(Color.WHITE);
        g2.fillRect(x, y, w, h);
        g2.setColor(new Color(45, 90, 140));
        g2.fillRect(x, y, w, headerH);
        g2.setColor(new Color(30, 30, 30));
        g2.setStroke(new BasicStroke(1.6f));
        g2.drawRect(x, y, w, h);
        g2.drawLine(x, y + headerH, x + w, y + headerH);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(tableName, x + (w - fm.stringWidth(tableName)) / 2, y + 22);

        g2.setColor(new Color(25, 25, 25));
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        for (int i = 0; i < cols.length; i++) {
            g2.drawString(cols[i], x + 10, y + headerH + 22 + i * 22);
        }
    }

    private static void drawErConnectorH(Graphics2D g2, int x1, int y1, int x2, int y2, String leftCard, String rightCard, String label) {
        g2.setColor(new Color(70, 70, 70));
        g2.setStroke(new BasicStroke(1.6f));
        g2.drawLine(x1, y1, x2, y2);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString(leftCard, x1 + 8, y1 - 6);
        g2.drawString(rightCard, x2 - 26, y2 - 6);
        g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
        int mx = (x1 + x2) / 2;
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, mx - fm.stringWidth(label) / 2, y1 - 6);
    }

    private static void drawErConnectorV(Graphics2D g2, int x1, int y1, int x2, int y2, String topCard, String bottomCard, String label) {
        g2.setColor(new Color(70, 70, 70));
        g2.setStroke(new BasicStroke(1.6f));
        g2.drawLine(x1, y1, x2, y2);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString(topCard, x1 + 8, y1 + 16);
        g2.drawString(bottomCard, x2 + 8, y2 - 8);
        g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
        int my = (y1 + y2) / 2;
        g2.drawString(label, x1 + 10, my + 4);
    }

    private static void drawLayerContainer(Graphics2D g2, int x, int y, int w, int h, String title, Color bg, Color border) {
        g2.setColor(bg);
        g2.fillRoundRect(x, y, w, h, 14, 14);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, w, h, 14, 14);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString(title, x + 16, y + 24);
    }

    private static void drawSmallNode(Graphics2D g2, int x, int y, int w, int h, String text, Color border) {
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x, y, w, h, 10, 10);
        g2.setColor(new Color(25, 25, 25));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String[] lines = text.split("\n");
        FontMetrics fm = g2.getFontMetrics();
        int startY = y + (h - lines.length * 16) / 2 + 12;
        for (int i = 0; i < lines.length; i++) {
            g2.drawString(lines[i], x + (w - fm.stringWidth(lines[i])) / 2, startY + i * 16);
        }
    }

    private static void drawArrowDown(Graphics2D g2, int x1, int y1, int x2, int y2, String label) {
        g2.setColor(new Color(80, 80, 80));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(x1, y1, x2, y2);
        int[] px = {x2, x2 - 6, x2 + 6};
        int[] py = {y2, y2 - 9, y2 - 9};
        g2.fillPolygon(px, py, 3);
        g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
        g2.drawString(label, x2 + 12, (y1 + y2) / 2 + 4);
    }

    private static void drawUmlClass(Graphics2D g2, int x, int y, int w, int h, String className, String[] fields, String[] methods, Color bg) {
        int headerH = 30;
        int fieldsH = Math.max(24, fields.length * 18 + 10);
        g2.setColor(bg);
        g2.fillRect(x, y, w, h);
        g2.setColor(new Color(40, 40, 40));
        g2.setStroke(new BasicStroke(1.6f));
        g2.drawRect(x, y, w, h);
        g2.drawLine(x, y + headerH, x + w, y + headerH);
        g2.drawLine(x, y + headerH + fieldsH, x + w, y + headerH + fieldsH);

        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(className, x + (w - fm.stringWidth(className)) / 2, y + 20);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        for (int i = 0; i < fields.length; i++) {
            g2.drawString(fields[i], x + 10, y + headerH + 18 + i * 18);
        }
        for (int i = 0; i < methods.length; i++) {
            g2.drawString(methods[i], x + 10, y + headerH + fieldsH + 18 + i * 18);
        }
    }
}
