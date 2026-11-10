# Studentu uzskaites sistēma

DTTT 2. projekta darbs (Programmas koda rakstīšana, Java). Darbvirsmas lietotne studentu, grupu un sekmju uzskaitei ar pieteikšanos sistēmā un lietotāju lomām.

**Autori:** Rinolds Baikovs (`RinoldsB`), Antons Liepa (`aantonsliepa301-ops`)  
**Grupa:** PR-32

## Tehnoloģijas

| Rīks | Versija / atrašanās vieta |
|------|---------------------------|
| Java (JDK) | 21 vai jaunāka |
| NetBeans IDE | Java SE (Ant) projekts, Swing GUI Builder |
| Apache Derby (Embedded) + JDBC | 10.17.1.0, mapē `src/lib/` (`derby.jar`, `derbyshared.jar`) |
| JUnit 4 + Hamcrest | 4.13.2 un 2.2, mapē `lib/` (`junit-4.13.2.jar`, `hamcrest-2.2.jar`) |
| Git / GitHub | Versiju kontrole |

Visas nepieciešamās bibliotēkas (`derby.jar`, `derbyshared.jar`, `junit-4.13.2.jar`, `hamcrest-2.2.jar`) ir iekļautas repozitorijā un NetBeans projektā (`nbproject/project.properties`) piesaistītas ar relatīviem ceļiem, tāpēc pēc klonēšanas citā datorā papildus nekas nav jālejupielādē.

## Kā palaist projektu

1. Uzstādīt **JDK 21+** un **Apache NetBeans IDE**.
2. Noklonēt repozitoriju (`git clone <repozitorija-URL>`) vai izpakot projekta arhīvu.
3. Atvērt NetBeans IDE: **File → Open Project...** un izvēlēties mapi `StudentuUzskaitesSistema`.
4. Palaist lietotni: **Run → Run Project** (vai taustiņš `F6`).
   - Galvenā (vienīgā `main`) klase: `studentuuzskaitesistema.gui.StudentuUzskaitesSistema`.
5. Palaist JUnit4 testus: **Run → Test Project** (vai `Alt+F6`).

## Datu bāze un sākuma lietotāju konti (pieslēgšanās dati)

Apache Derby Embedded datu bāze `StudentuUzskaitesDB` un sākuma dati tiek izveidoti **automātiski pirmajā programmas palaišanas reizē** (`DBConnection.initialize()`).

Pieslēgšanās logā var izmantot šādus noklusējuma kontus:

| Lietotājvārds | Parole | Loma | Tiesības sistēmā |
|---------------|--------|------|------------------|
| `admin` | `admin123` | `ADMIN` (Lietvedis / administrators) | Pilna piekļuve: studentu un grupu pievienošana, labošana, dzēšana, meklēšana, pasniedzēju piešķiršana grupām, sekmju un atskaišu apskate. |
| `pasniedzejs` | `teacher123` | `TEACHER` (Pasniedzējs) | Piekļuve savām grupām: savu grupu studentu apskate un meklēšana, sekmju ievade, labošana un dzēšana, sekmju atskaites sagatavošana. |

Jaunu pasniedzēja kontu var izveidot arī pieslēgšanās logā, nospiežot pogu **«Reģistrēties»**.

## Projekta struktūra

```text
StudentuUzskaitesSistema/
├── src/
│   ├── lib/
│   │   ├── derby.jar
│   │   └── derbyshared.jar
│   └── studentuuzskaitesistema/
│       ├── dao/     GradeDAO.java, GroupDAO.java, StudentDAO.java, UserDAO.java
│       ├── db/      DBConnection.java
│       ├── gui/     StudentuUzskaitesSistema.java (main), LoginFrame.java (.form)
│       ├── model/   Grade.java, Student.java, StudentGroup.java, User.java
│       └── util/    PasswordUtil.java, Session.java, Validator.java
├── test/
│   └── studentuuzskaitesistema/
│       ├── dao/     GradeDAOTest.java
│       └── util/    ValidatorTest.java
├── lib/
│   ├── junit-4.13.2.jar
│   └── hamcrest-2.2.jar
├── docs/            Ģenerētā Javadoc dokumentācija
└── README.md
```
