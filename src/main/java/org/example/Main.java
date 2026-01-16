package org.example;

public class Main {
    static OutputDevice output = new OutputDevice(System.out);
    static InputDevice input = new InputDevice(System.in);
    static Application app = new Application(output, input);

    /**
     * Function that creates a user with the name and password given as arguments.
     * @param name Name of the user, given as first argument
     * @param password Password of the user, given as the second argument
     * @param isAdmin Role of user, managed by the program
     */
    public static void createUser(String name, String password, boolean isAdmin) {
        output.writeMessage("   --Creating a new account--\n");
        output.writeMessage(" Name: " + name + "\n");

        // checking for exceptions
        try {
            app.nameCheck(name);
        }
        catch (IllegalNameException e) {
            output.writeMessage(e.getMessage());
            return;
        }

        output.writeMessage(" Password: " + password + "\n");

        try {
            app.passwordCheck(password);
        }
        catch (IllegalPasswordException e) {
            output.writeMessage(e.getMessage());
            return;
        }

        org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
        org.hibernate.Transaction tx = session.beginTransaction();

        try {
            Long count = session.createQuery(
                            "select count(u) from User u where lower(u.Username) = lower(:uname)",
                            Long.class
                    )
                    .setParameter("uname", name)
                    .uniqueResult();

            if (count != null && count > 0) {
                output.writeMessage("An account with that username already exists!\n");
                tx.rollback();
                session.close();
                return;
            }

            User user = new User(name, password, isAdmin);

            BankAccount account = new BankAccount();
            account.setUser(user);
            account.setBalance(0.0);

            session.persist(user);
            session.persist(account);

            tx.commit();
            output.writeMessage("Account created successfully!\n");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            output.writeMessage("Failed to create account.\n");
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    /**
     Function that prints on screen all the functions of the program and their respective arguments.
     */
    public static void printUsage() {
        output.writeMessage("""
                Options:
                    -cu <name> <password>   Creates a new user with the given name and password
                    -ca <name> <password>   Creates a new admin with the given name and password
                    -ea <name> <password>   Enters an account using the name and password as input
                    -vi <name> <password>   Shows all account, coupon and transaction information of the Bank,
                                            requires admin privileges
                """);
    }


    /**
     Helper function that prints on screen all coupons in Bank, all indexed.
     */
    public static void printCoupons() {
        org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            java.util.List<Coupon> coupons = session
                    .createQuery("from Coupon c order by c.store asc", Coupon.class)
                    .list();

            if (coupons.isEmpty()) {
                output.writeMessage("No coupons to display!\n");
                return;
            }

            int index = 1;
            for (Coupon coupon : coupons) {
                output.writeMessage(index + ". " + coupon.toString() + "\n");
                index++;
            }

        } catch (Exception e) {
            output.writeMessage("Failed to load coupons (DB error).\n");
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    /**
     * Helper function that allows the changing of the info of an exiting coupon.
     */
    public static void changeCouponInfo() {
        while (true) {
            output.writeMessage("   --Changing a coupon--\n");
            output.writeMessage("   Choose coupon to modify:\n");

            org.hibernate.Session sessionList = HibernateUtil.getSessionFactory().openSession();
            java.util.List<Coupon> coupons;
            try {
                coupons = sessionList
                        .createQuery("from Coupon c order by c.store asc", Coupon.class)
                        .list();
            } finally {
                sessionList.close();
            }

            if (coupons.isEmpty()) {
                output.writeMessage("No coupons to modify!\n");
                return;
            }

            for (int i = 0; i < coupons.size(); i++) {
                output.writeMessage((i + 1) + ". " + coupons.get(i).toString() + "\n");
            }

            output.writeMessage("Or type 0 to quit\n");
            output.writeMessage("Input: ");
            int choice = input.readInt();

            if (choice == 0) {
                output.writeMessage("Canceling...\n");
                return;
            }
            if (choice < 1 || choice > coupons.size()) {
                output.writeMessage(choice + " is invalid\n");
                continue;
            }

            Coupon selected = coupons.get(choice - 1);
            Integer selectedId = selected.getId();

            while (true) {
                output.writeMessage("Edit: 1.Store or 2.Percentage? Or type 0 to quit \nInput: ");
                String editInput = input.readLine();

                if (editInput.equalsIgnoreCase("0")
                        || editInput.equalsIgnoreCase("cancel")
                        || editInput.equalsIgnoreCase("0.cancel")) {
                    output.writeMessage("Canceling...\n");
                    break;
                }

                if (editInput.equalsIgnoreCase("1")
                        || editInput.equalsIgnoreCase("store")
                        || editInput.equalsIgnoreCase("1.store")) {
                    String newStore;
                    while(true) {
                        output.writeMessage("Input new store: ");
                        newStore = input.readLine();

                        if (newStore.isBlank()) {
                            output.writeMessage("Store name cannot be blank! Please enter a valid store name!\n");
                            continue;
                        }
                        break;
                    }

                    org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
                    org.hibernate.Transaction tx = session.beginTransaction();

                    try {
                        Coupon managed = session.find(Coupon.class, selectedId);
                        if (managed == null) {
                            output.writeMessage("Coupon no longer exists!\n");
                            tx.rollback();
                            break;
                        }

                        managed.setStore(newStore);

                        tx.commit();
                        output.writeMessage("Coupon updated successfully!\n");
                    } catch (Exception e) {
                        tx.rollback();
                        output.writeMessage("Failed to update coupon (DB error).\n");
                        throw new RuntimeException(e);
                    } finally {
                        session.close();
                    }

                    break;
                }

                else if (editInput.equalsIgnoreCase("2")
                        || editInput.equalsIgnoreCase("percentage")
                        || editInput.equalsIgnoreCase("2.percentage")) {
                    int newPercentage;
                    while (true) {
                        output.writeMessage("Input new percentage: ");
                        newPercentage = input.readInt();

                        if (newPercentage <= 0 || newPercentage > 100) {
                            output.writeMessage("Invalid percentage! Cannot be <= 0 or over 100%.\n");
                            continue;
                        }
                        break;
                    }


                    org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
                    org.hibernate.Transaction tx = session.beginTransaction();

                    try {
                        Coupon managed = session.find(Coupon.class, selectedId);
                        if (managed == null) {
                            output.writeMessage("Coupon no longer exists!\n");
                            tx.rollback();
                            break;
                        }

                        managed.setPercentage(newPercentage);

                        tx.commit();
                        output.writeMessage("Coupon updated successfully!\n");
                    } catch (Exception e) {
                        tx.rollback();
                        output.writeMessage("Failed to update coupon (DB error).\n");
                        throw new RuntimeException(e);
                    } finally {
                        session.close();
                    }

                    break;
                }

                else {
                    output.writeMessage("Invalid choice!\n");
                }
            }
        }
    }

    public static void deleteCoupon() {
        while (true) {
            output.writeMessage("   --Deleting a coupon--\n");
            output.writeMessage("   Choose coupon to delete:\n");

            org.hibernate.Session sessionList = HibernateUtil.getSessionFactory().openSession();
            java.util.List<Coupon> coupons;
            try {
                coupons = sessionList
                        .createQuery("from Coupon c order by c.store asc", Coupon.class)
                        .list();
            } finally {
                sessionList.close();
            }

            if (coupons.isEmpty()) {
                output.writeMessage("No coupons to delete!\n");
                return;
            }


            for (int i = 0; i < coupons.size(); i++) {
                output.writeMessage((i + 1) + ". " + coupons.get(i).toString() + "\n");
            }

            output.writeMessage("Or type 0 to quit\n");
            output.writeMessage("Input: ");
            int choice = input.readInt();

            if (choice == 0) {
                output.writeMessage("Canceling...\n");
                return;
            }
            if (choice < 1 || choice > coupons.size()) {
                output.writeMessage(choice + " is invalid\n");
                continue;
            }

            Integer couponId = coupons.get(choice - 1).getId();

            org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
            org.hibernate.Transaction tx = session.beginTransaction();

            try {
                Coupon managed = session.find(Coupon.class, couponId);
                if (managed == null) {
                    output.writeMessage("Coupon no longer exists!\n");
                    tx.rollback();
                    continue;
                }

                session.remove(managed);

                tx.commit();
                output.writeMessage("Coupon deleted successfully!\n");

            } catch (Exception e) {
                if (tx != null) tx.rollback();

                output.writeMessage("""
                    Could not delete coupon.
                    It may be used by existing transactions (foreign key constraint).
                    Delete/Update those transactions first, or allow NULL coupon_id.
                    """);

                throw new RuntimeException(e);

            } finally {
                session.close();
            }
        }
    }

    /**
     * Function that allows the modification of coupons. It lets the user: add new coupons, modify existing ones and
     * to delete exiting ones.
     */
    public static void modifyCoupons(){
        while (true) {
            output.writeMessage("""
                       --Modifying coupons: --
                    1.Add coupon
                    2.Modify coupon
                    3.Delete coupon
                    4.Exit
                    Input:\s""");
            String couponChoice = input.readLine();

            if (couponChoice.equals("1") || couponChoice.equalsIgnoreCase("add")
                    || couponChoice.equalsIgnoreCase("1.Add coupon")) {
                output.writeMessage("Coupon store: ");
                String store =  input.readLine();

                output.writeMessage("% off: ");
                int percentage = input.readInt();

                org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
                org.hibernate.Transaction tx = session.beginTransaction();

                try {
                    Coupon c = new Coupon(store, percentage);
                    session.persist(c);
                    tx.commit();
                    output.writeMessage("Coupon added!\n");
                } catch (Exception e) {
                    if (tx != null) tx.rollback();
                    output.writeMessage("Failed to add coupon (DB error).\n");
                    throw new RuntimeException(e);
                } finally {
                    session.close();
                }


            } else if (couponChoice.equals("2") || couponChoice.equalsIgnoreCase("modify")
                    || couponChoice.equalsIgnoreCase("1.Modify coupon")) {
                changeCouponInfo();

            } else if (couponChoice.equals("3") || couponChoice.equalsIgnoreCase("delete")
                    || couponChoice.equalsIgnoreCase("1.Delete coupon")) {
                deleteCoupon();

            } else if (couponChoice.equals("4") || couponChoice.equalsIgnoreCase("4.Exit")
                    || couponChoice.equalsIgnoreCase("exit")) {
                output.writeMessage("Exiting...\n");
                break;
            } else {
                output.writeMessage("Invalid choice!");
            }
        }
    }


    /**
     Helper function that prints on screen all accounts in Bank, all indexed.
     */
    public static void printAccounts() {
        org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            java.util.List<BankAccount> accounts = session
                    .createQuery("from BankAccount a order by a.accountID asc", BankAccount.class)
                    .list();

            if (accounts.isEmpty()) {
                output.writeMessage("No accounts to display!\n");
                return;
            }

            int index = 1;
            for (BankAccount account : accounts) {
                output.writeMessage(index + ". " + account.toString() + "\n");
                index++;
            }

        } catch (Exception e) {
            output.writeMessage("Failed to load accounts (DB error).\n");
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }


    private static java.util.List<BankAccount> loadAccountsForSelection() {
        org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            java.util.List<BankAccount> accounts = session
                    .createQuery("from BankAccount a order by a.accountID asc", BankAccount.class)
                    .list();

            if (accounts.isEmpty()) {
                output.writeMessage("No accounts to modify!\n");
            } else {
                for (int i = 0; i < accounts.size(); i++) {
                    output.writeMessage((i + 1) + ". " + accounts.get(i).toString() + "\n");
                }
            }
            return accounts;
        } finally {
            session.close();
        }
    }

    private static void updateAccountBalance(int accountId, double newBalance) {
        org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
        org.hibernate.Transaction tx = session.beginTransaction();

        try {
            BankAccount acc = session.find(BankAccount.class, accountId);
            if (acc == null) {
                output.writeMessage("Account no longer exists!\n");
                tx.rollback();
                return;
            }

            acc.setBalance(newBalance);

            tx.commit();
            output.writeMessage("Balance updated successfully!\n");
        } catch (Exception e) {
            tx.rollback();
            output.writeMessage("Failed to update balance.\n");
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    private static void updateAccountUsername(int accountId, String newName) {
        org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
        org.hibernate.Transaction tx = session.beginTransaction();

        try {
            BankAccount acc = session.find(BankAccount.class, accountId);
            if (acc == null) {
                output.writeMessage("Account no longer exists!\n");
                tx.rollback();
                return;
            }

            acc.getUser().setUsername(newName);

            tx.commit();
            output.writeMessage("Name updated successfully!\n");
        } catch (Exception e) {
            tx.rollback();
            output.writeMessage("Failed to update name.\n");
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    private static void updateAccountPassword(int accountId, String newPassword) {
        org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
        org.hibernate.Transaction tx = session.beginTransaction();

        try {
            BankAccount acc = session.find(BankAccount.class, accountId);
            if (acc == null) {
                output.writeMessage("Account no longer exists!\n");
                tx.rollback();
                return;
            }

            acc.getUser().setPassword(newPassword);

            tx.commit();
            output.writeMessage("Password updated successfully!\n");
        } catch (Exception e) {
            tx.rollback();
            output.writeMessage("Failed to update password.\n");
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    private static void setAdminStatus(int accountId, boolean makeAdmin) {
        org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
        org.hibernate.Transaction tx = session.beginTransaction();

        try {
            BankAccount acc = session.find(BankAccount.class, accountId);
            if (acc == null) {
                output.writeMessage("Account no longer exists!\n");
                tx.rollback();
                return;
            }

            acc.getUser().setAdmin(makeAdmin);

            tx.commit();
            output.writeMessage(makeAdmin ? "User promoted successfully!\n" : "User demoted successfully!\n");
        } catch (Exception e) {
            tx.rollback();
            output.writeMessage("Failed to update admin status.\n");
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    /**
     * Helper function that changes the information of a chosen account. Can change the balance, name, password or
     * admin account status.
     */
    public static void changeAccountInfo() {
        while (true) {
            output.writeMessage("   --Changing an account--\n");
            output.writeMessage("   Choose account to modify:\n");

            java.util.List<BankAccount> accounts = loadAccountsForSelection();
            if (accounts.isEmpty()) return;

            output.writeMessage("Or type 0 to exit\n");
            output.writeMessage("Input: ");
            int choice = input.readInt();

            if (choice == 0) {
                output.writeMessage("Exiting...\n");
                return;
            }
            if (choice < 1 || choice > accounts.size()) {
                output.writeMessage("Invalid choice!\n");
                continue;
            }

            int chosenAccountId = accounts.get(choice - 1).getAccountID();

            while (true) {
                output.writeMessage(
                        "Edit: 1.Balance, 2.Name, 3.Password or 4.Promote/Demote? Or type 0 to exit\nInput: "
                );
                String editInput = input.readLine();

                if (editInput.equalsIgnoreCase("0")
                        || editInput.equalsIgnoreCase("cancel")
                        || editInput.equalsIgnoreCase("0.cancel")) {
                    output.writeMessage("Canceling...\n");
                    break;
                }

                if (editInput.equalsIgnoreCase("1")
                        || editInput.equalsIgnoreCase("balance")
                        || editInput.equalsIgnoreCase("1.balance")) {

                    while (true) {
                        output.writeMessage("Input new balance: ");
                        double newBalance = input.readDouble();

                        if (newBalance < 0) {
                            output.writeMessage("New balance cannot be negative!\n");
                            continue;
                        }

                        updateAccountBalance(chosenAccountId, newBalance);
                        break;
                    }
                }

                else if (editInput.equalsIgnoreCase("2")
                        || editInput.equalsIgnoreCase("name")
                        || editInput.equalsIgnoreCase("2.name")) {

                    while (true) {
                        output.writeMessage("Input new name: ");
                        String newName = input.readLine();

                        if (newName == null || newName.isBlank()) {
                            output.writeMessage("New name cannot be empty!\n");
                            continue;
                        }

                        updateAccountUsername(chosenAccountId, newName);
                        break;
                    }
                }

                else if (editInput.equalsIgnoreCase("3")
                        || editInput.equalsIgnoreCase("password")
                        || editInput.equalsIgnoreCase("3.password")) {

                    while (true) {
                        output.writeMessage("Input new password: ");
                        String newPassword = input.readLine();

                        if (newPassword == null || newPassword.isBlank()) {
                            output.writeMessage("New password cannot be empty!\n");
                            continue;
                        }
                        // Bypasses some restrictions on purpose, for making something like a debug account

                        updateAccountPassword(chosenAccountId, newPassword);
                        break;
                    }
                }

                else if (editInput.equalsIgnoreCase("4")
                        || editInput.equalsIgnoreCase("promote")
                        || editInput.equalsIgnoreCase("4.promote")) {

                    org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
                    boolean isAdminNow;
                    try {
                        BankAccount acc = session.find(BankAccount.class, chosenAccountId);
                        if (acc == null) {
                            output.writeMessage("Account no longer exists!\n");
                            break;
                        }
                        isAdminNow = acc.getUser().isAdmin();
                    } finally {
                        session.close();
                    }

                    if (isAdminNow) {
                        while (true) {
                            output.writeMessage("User is already admin, demote to regular user? Y/N\nInput: ");
                            String decision = input.readLine();

                            if (decision.equalsIgnoreCase("y") || decision.equalsIgnoreCase("yes")) {
                                setAdminStatus(chosenAccountId, false);
                                break;
                            } else if (decision.equalsIgnoreCase("n") || decision.equalsIgnoreCase("no")) {
                                output.writeMessage("Aborting...\n");
                                break;
                            } else {
                                output.writeMessage("Invalid choice!\n");
                            }
                        }
                    } else {
                        while (true) {
                            output.writeMessage("User is not admin, promote user to admin? Y/N\nInput: ");
                            String decision = input.readLine();

                            if (decision.equalsIgnoreCase("y") || decision.equalsIgnoreCase("yes")) {
                                setAdminStatus(chosenAccountId, true);
                                break;
                            } else if (decision.equalsIgnoreCase("n") || decision.equalsIgnoreCase("no")) {
                                output.writeMessage("Aborting...\n");
                                break;
                            } else {
                                output.writeMessage("Invalid choice!\n");
                            }
                        }
                    }
                }

                else {
                    output.writeMessage("Invalid choice!\n");
                }
            }
        }
    }

    /**
     * Helper function that deletes a chosen account.
     */
    public static void deleteAccount() {
        while (true) {
            output.writeMessage("   --Deleting an account--\n");
            output.writeMessage("   Choose account to delete:\n");

            java.util.List<BankAccount> accounts = loadAccountsForSelection();
            if (accounts.isEmpty()) return;

            output.writeMessage("Or type 0 to exit\n");
            output.writeMessage("Input: ");
            int choice = input.readInt();

            if (choice == 0) {
                output.writeMessage("Exiting...\n");
                return;
            }
            if (choice < 1 || choice > accounts.size()) {
                output.writeMessage("Invalid choice!\n");
                continue;
            }

            int chosenAccountId = accounts.get(choice - 1).getAccountID();

            while (true) {
                output.writeMessage("Delete all transactions associated with this account? Y/N\nInput: ");
                String decision = input.readLine();

                boolean deleteTx;
                if (decision.equalsIgnoreCase("y") || decision.equalsIgnoreCase("yes")) {
                    deleteTx = true;
                } else if (decision.equalsIgnoreCase("n") || decision.equalsIgnoreCase("no")) {
                    deleteTx = false;
                } else {
                    output.writeMessage("Invalid choice!\n");
                    continue;
                }

                org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
                org.hibernate.Transaction tx = session.beginTransaction();

                try {
                    BankAccount managedAcc = session.find(BankAccount.class, chosenAccountId);
                    if (managedAcc == null) {
                        output.writeMessage("Account no longer exists!\n");
                        tx.rollback();
                        break;
                    }

                    if (deleteTx) {
                        int deleted = session.createMutationQuery(
                                        "delete from Transaction t where t.accountID = :aid"
                                ).setParameter("aid", chosenAccountId)
                                .executeUpdate();

                        output.writeMessage("Transactions deleted successfully! (" + deleted + ")\n");
                    }

                    session.remove(managedAcc);

                    tx.commit();
                    output.writeMessage("Account deleted successfully!\n");
                    break;

                } catch (Exception e) {
                    if (tx != null) tx.rollback();

                    output.writeMessage("""
                        Failed to delete account (DB error).
                        If you chose NOT to delete transactions, the database may block deletion because transactions reference this account.
                        Try again and delete transactions as well.
                        """);

                    throw new RuntimeException(e);
                } finally {
                    session.close();
                }
            }
        }
    }

    /**
     * Function that allows the modification or deletion of some chosen account.
     */
    public static void modifyAccounts(){
        org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Long count = session.createQuery("select count(a) from BankAccount a", Long.class)
                    .uniqueResult();

            if (count == null || count == 0) {
                output.writeMessage("No accounts to modify!\n");
                return;
            }
        } finally {
            session.close();
        }

        while (true) {
            output.writeMessage("""
                       --Modifying accounts: --
                    1.Modify account
                    2.Delete account
                    3.Exit
                    Input:\s""");
            String accountChoice = input.readLine();

            if (accountChoice.equals("1") || accountChoice.equalsIgnoreCase("modify")
                    || accountChoice.equalsIgnoreCase("1.Modify account")) {
                changeAccountInfo();

            } else if (accountChoice.equals("2") || accountChoice.equalsIgnoreCase("delete")
                    || accountChoice.equalsIgnoreCase("2.Delete account")) {
                deleteAccount();

            } else  if (accountChoice.equals("3") || accountChoice.equalsIgnoreCase("exit")
                    || accountChoice.equalsIgnoreCase("3.Exit")) {
                break;
            } else  {
                output.writeMessage("Invalid choice!\n");
            }
        }
    }

    /**
     Helper function that prints on screen all the transactions of every account in Bank, all indexed.
     */
    public static void printTransactions() {
        org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            java.util.List<Transaction> txs = session
                    .createQuery("from Transaction t order by t.date asc", Transaction.class)
                    .list();

            if (txs.isEmpty()) {
                output.writeMessage("No transactions to display!\n");
                return;
            }

            int index = 1;
            for (Transaction t : txs) {
                output.writeMessage(index + ". " + t.toString() + "\n");
                index++;
            }

        } catch (Exception e) {
            output.writeMessage("Failed to load transactions (DB error).\n");
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }


    /**
     * Helper function that returns the account associated to the name and password given as arguments, or "null" if the
     * account is not found.
     * @param accountName Name of an account
     * @param accountPassword Password of the account
     */
    public static User getCurrentUser(String accountName, String accountPassword) {

        org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            // Find user by username (case-insensitive)
            User user = session.createQuery(
                            "from User u where lower(u.Username) = lower(:uname)",
                            User.class
                    ).setParameter("uname", accountName)
                    .uniqueResult();

            if (user == null) {
                output.writeMessage("No such account: " + accountName);
                return null;
            }

            // Password check (same as your current logic)
            if (!user.checkPassword(accountPassword)) {
                output.writeMessage("Incorrect password!");
                return null;
            }

            return user;

        } catch (Exception e) {
            output.writeMessage("Login failed (DB error).\n");
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }


    /**
     * Function that takes as argument name and password of an admin account and then prints out bank information such
     * as: all accounts, all transactions, all coupons and the file path of the file where the information is stored.
     * @param accountName Name of an admin account
     * @param accountPassword Password of the admin account
     */
    public static void viewInfo(String accountName, String accountPassword) {
        // admin check
        User currentAccount = getCurrentUser(accountName, accountPassword);

        if (currentAccount == null) {
            return; // message already printed
        }

        if (!currentAccount.isAdmin()) {
            output.writeMessage("Account is not admin! This function requires admin privileges.");
            return;
        }

        while (true) {
            output.writeMessage("Welcome admin!\n");
            output.writeMessage("""
                    --Viewing saved information: --
                1.View accounts
                2.View transactions
                3.View coupons
                4.Exit
                """);
            output.writeMessage("Input: ");
            String choice = input.readLine();

            if (choice.equals("4") || choice.equalsIgnoreCase("4.exit")
                    || choice.equalsIgnoreCase("exit")) {
                break;
            }

            if (choice.equalsIgnoreCase("1") || choice.equalsIgnoreCase("view accounts")
                    || choice.equalsIgnoreCase("1.view accounts")) {

                printAccounts();
                output.writeMessage("Press enter to continue.\n");
                input.readLine();

            } else if (choice.equalsIgnoreCase("2") || choice.equalsIgnoreCase("view transactions")
                    || choice.equalsIgnoreCase("2.view transactions")) {

                printTransactions();
                output.writeMessage("Press enter to continue.\n");
                input.readLine();

            } else if (choice.equalsIgnoreCase("3") || choice.equalsIgnoreCase("view coupons")
                    || choice.equalsIgnoreCase("3.view coupons")) {

                printCoupons();
                output.writeMessage("Press enter to continue.\n");
                input.readLine();

            } else {
                output.writeMessage("Invalid choice!\n");
            }
        }
    }


    /**
     * Helper function that prints out all transactions associated to the user given as parameter.
     * @param account Account of which to print the transactions of
     */
    public static void printUserTransactions(BankAccount account) {
        org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            java.util.List<Transaction> txs = session.createQuery(
                            "from Transaction t where t.accountID = :aid order by t.date asc",
                            Transaction.class
                    ).setParameter("aid", account.getAccountID())
                    .list();

            if (txs.isEmpty()) {
                output.writeMessage("No transactions found.\n");
                return;
            }

            int index = 1;
            for (Transaction t : txs) {
                output.writeMessage(index + ". " + t.toString() + "\n");
                index++;
            }

        } catch (Exception e) {
            output.writeMessage("Failed to load transactions (DB error).\n");
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    /**
     * Helper function that lets the user make a deposit to their bank account. The user inputs the amount to
     * deposit and the deposit is automatically saved and associated to their account.
     * @param account Account of which to associate the deposit to
     */
    public static void makeDeposit(BankAccount account) {
        while (true) {
            output.writeMessage("Input amount to deposit to account or 0 to cancel: ");
            double inputAmount = input.readDouble();

            if (inputAmount == 0) {
                output.writeMessage("Canceling...\n");
                return;
            }
            if (inputAmount < 0) {
                output.writeMessage("Invalid amount! Cannot deposit negative amount!\n");
                continue;
            }

            org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
            org.hibernate.Transaction tx = session.beginTransaction();

            try {
                BankAccount managedAcc = session.find(BankAccount.class, account.getAccountID());

                if (managedAcc == null) {
                    output.writeMessage("Account no longer exists!\n");
                    tx.rollback();
                    return;
                }


                Deposit d = new Deposit();
                d.setAccountID(managedAcc.getAccountID());
                d.setBalance(inputAmount);
                d.setDate(java.time.LocalDateTime.now());
                session.persist(d);

                managedAcc.updateBalance(inputAmount);

                tx.commit();

                account.setBalance(managedAcc.getBalance());

                output.writeMessage("Deposit successful!\n");
                return;

            } catch (Exception e) {
                if (tx != null) tx.rollback();
                output.writeMessage("Deposit failed.\n");
                throw new RuntimeException(e);
            } finally {
                session.close();
            }
        }
    }


    /**
     * Helper function that lets the user make a withdrawal from their bank account. The user inputs the amount to
     * withdraw and the withdrawal is automatically saved and associated to their account.
     * @param account Account of which to associate the withdrawal to
     */
    public static void makeWithdrawal(BankAccount account) {
        while (true) {
            output.writeMessage("Input amount to withdraw from account or 0 to cancel: ");
            double inputAmount = input.readDouble();

            if (inputAmount == 0) {
                output.writeMessage("Canceling...\n");
                return;
            }
            if (inputAmount < 0) {
                output.writeMessage("Invalid amount! Cannot withdraw negative amount!\n");
                continue;
            }

            org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
            org.hibernate.Transaction tx = session.beginTransaction();

            try {
                BankAccount managedAcc = session.find(BankAccount.class, account.getAccountID());
                if (managedAcc == null) {
                    output.writeMessage("Account no longer exists!\n");
                    tx.rollback();
                    return;
                }

                if (inputAmount > managedAcc.getBalance()) {
                    output.writeMessage("Invalid amount! Cannot withdraw more than the account balance!\n");
                    tx.rollback();
                    return;
                }

                Withdrawal w = new Withdrawal();
                w.setAccountID(managedAcc.getAccountID());
                w.setBalance(inputAmount);
                w.setDate(java.time.LocalDateTime.now());
                session.persist(w);

                managedAcc.updateBalance(-inputAmount);

                tx.commit();

                account.setBalance(managedAcc.getBalance());

                output.writeMessage("Withdrawal successful!\n");
                return;

            } catch (Exception e) {
                if (tx != null) tx.rollback();
                output.writeMessage("Withdrawal failed.\n");
                throw new RuntimeException(e);
            } finally {
                session.close();
            }
        }
    }

    /**
     * Helper function that lets the user make a transfer to another bank account. The user inputs the account ID to
     * transfer to and then the amount. The transfer is automatically saved and associated to the account of the user
     * who made it.
     * @param account Account of which to associate the transfer to
     */
    public static void makeTransfer(BankAccount account) {
        while (true) {
            output.writeMessage("Input account ID to transfer to or 0 to cancel: ");
            int otherAccountId = input.readInt();

            if (otherAccountId == 0) {
                output.writeMessage("Canceling...\n");
                return;
            }

            output.writeMessage("Input amount to transfer or 0 to cancel: ");
            double inputAmount = input.readDouble();

            if (inputAmount == 0) {
                output.writeMessage("Canceling...\n");
                return;
            }
            if (inputAmount < 0) {
                output.writeMessage("Invalid amount! Cannot transfer negative amount!\n");
                continue;
            }

            org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
            org.hibernate.Transaction tx = session.beginTransaction();

            try {
                BankAccount source = session.find(BankAccount.class, account.getAccountID());
                if (source == null) {
                    output.writeMessage("Your account no longer exists!\n");
                    tx.rollback();
                    return;
                }

                BankAccount target = session.find(BankAccount.class, otherAccountId);
                if (target == null) {
                    output.writeMessage("Invalid account ID!\n");
                    tx.rollback();
                    return;
                }

                if (source.getAccountID() == target.getAccountID()) {
                    output.writeMessage("Invalid account ID! Cannot transfer to the same account.\n");
                    tx.rollback();
                    return;
                }

                if (inputAmount > source.getBalance()) {
                    output.writeMessage("Invalid amount! Cannot transfer more than the account balance!\n");
                    tx.rollback();
                    return;
                }

                Transfer t = new Transfer();
                t.setAccountID(source.getAccountID());
                t.setTargetAccountId(target.getAccountID());
                t.setBalance(inputAmount);
                t.setDate(java.time.LocalDateTime.now());
                session.persist(t);

                source.updateBalance(-inputAmount);
                target.updateBalance(inputAmount);

                tx.commit();

                account.setBalance(source.getBalance());

                output.writeMessage("Transfer successful!\n");
                return;

            } catch (Exception e) {
                if (tx != null) tx.rollback();
                output.writeMessage("Transfer failed (DB error).\n");
                throw new RuntimeException(e);
            } finally {
                session.close();
            }
        }
    }

    /**
     * Helper function that lets the user make a purchase from a store and automatically applies any applicable coupons.
     * The user inputs the name of the store and the total spent. Automatically saves and associates the purchase to the
     * account given as argument.
     * @param account Account of which to associate the purchase to
     */
    public static void makePurchase(BankAccount account) {
        while (true) {
            output.writeMessage("Input store name or leave blank to cancel: ");
            String storeName = input.readLine();

            if (storeName.isBlank()) {
                output.writeMessage("Canceling...\n");
                return;
            }

            output.writeMessage("Input purchase total amount or 0 to cancel: ");
            double inputAmount = input.readDouble();

            if (inputAmount == 0) {
                output.writeMessage("Canceling...\n");
                return;
            }
            if (inputAmount < 0) {
                output.writeMessage("Invalid amount! Amount cannot be negative!\n");
                continue;
            }

            org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
            org.hibernate.Transaction tx = session.beginTransaction();

            try {
                BankAccount managedAcc = session.find(BankAccount.class, account.getAccountID());
                if (managedAcc == null) {
                    output.writeMessage("Account no longer exists!\n");
                    tx.rollback();
                    return;
                }

                if (inputAmount > managedAcc.getBalance()) {
                    output.writeMessage("Invalid amount! Cannot purchase more than the account balance!\n");
                    tx.rollback();
                    return;
                }

                Coupon foundCoupon = session.createQuery(
                                "from Coupon c where lower(c.store) = lower(:store)",
                                Coupon.class
                        ).setParameter("store", storeName)
                        .uniqueResult();

                double finalAmount = inputAmount;
                if (foundCoupon == null) {
                    output.writeMessage("No coupons found...\n");
                } else {
                    int percent = foundCoupon.getPercentage();
                    java.math.BigDecimal amountOff = java.math.BigDecimal.valueOf((inputAmount * percent) / 100.0);
                    double dAmountOff = amountOff.setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();

                    java.math.BigDecimal finalTotal = java.math.BigDecimal.valueOf(inputAmount - dAmountOff);
                    finalAmount = finalTotal.setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();

                    output.writeMessage("Coupon found! You saved %.2f!\n".formatted(dAmountOff));
                }


                Purchase p = new Purchase();
                p.setAccountID(managedAcc.getAccountID());
                p.setBalance(finalAmount);
                p.setStore(storeName);
                p.setDate(java.time.LocalDateTime.now());
                p.setAppliedCoupon(foundCoupon == null ? null : foundCoupon.getId());
                session.persist(p);

                managedAcc.updateBalance(-finalAmount);

                tx.commit();

                account.setBalance(managedAcc.getBalance());

                output.writeMessage("Purchase successful!\n");
                return;

            } catch (Exception e) {
                if (tx != null) tx.rollback();
                output.writeMessage("Purchase failed (DB error).\n");
                throw new RuntimeException(e);
            } finally {
                session.close();
            }
        }
    }

    /**
     * Helper function that prints out the information of the account given as argument. Prints out: username,
     * balance and account ID.
     * @param account Account of which to print the info of
     */
    public static void printAccountInfo(BankAccount account) {
        output.writeMessage("   --Account info:\n");
        output.writeMessage("Username: " + account.getUsername() + "\n");
        output.writeMessage("Balance: " + account.getBalance() + "\n");
        output.writeMessage("Account ID: " + account.getAccountID() + "\n");
        output.writeMessage("Press enter to continue...");
        input.readLine();
    }
    /**
     * Function that enters into an existing account with the name and password given as arguments. If the account is
     * an admin, the function lets them manage the bank app. If account is a normal user, it lets them view information
     * about their account as well as do transactions.
     * @param accountName Name of the user, given as first argument
     * @param accountPassword Password of the user, given as the second argument
     */
    public static void enterAccount(String accountName, String accountPassword) {

        User currentAccount = getCurrentUser(accountName, accountPassword);
        if (currentAccount == null) {
            return;
        }

        if (currentAccount.isAdmin()) {
            while (true) {
                output.writeMessage("Welcome admin!\n");
                output.writeMessage("   --Modify: -- \n1.Coupons\n2.Accounts\n3.Stop modifying\nInput: ");
                String choice = input.readLine();

                if (choice.equals("1") || choice.equalsIgnoreCase("coupons")
                        || choice.equalsIgnoreCase("1.coupons")) {
                    modifyCoupons();
                } else if (choice.equals("2") || choice.equalsIgnoreCase("accounts")
                        || choice.equalsIgnoreCase("2.accounts")) {
                    modifyAccounts();
                } else if (choice.equals("3") || choice.equalsIgnoreCase("Stop modifying")
                        || choice.equalsIgnoreCase("3.Stop")) {
                    output.writeMessage("Stopping...\n");
                    break;
                } else {
                    output.writeMessage("Invalid choice!\n");
                }
            }
            return;
        }

        BankAccount currentBankAccount;

        org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            currentBankAccount = session.createQuery(
                            "from BankAccount a where a.User.id = :uid",
                            BankAccount.class
                    )
                    .setParameter("uid", currentAccount.getId())
                    .uniqueResult();
        } catch (Exception e) {
            output.writeMessage("Failed to load account (DB error).\n");
            throw new RuntimeException(e);
        } finally {
            session.close();
        }

        if (currentBankAccount == null) {
            output.writeMessage("No bank account found for this user!\n");
            return;
        }

        while (true) {
            output.writeMessage("Welcome %s!\n".formatted(accountName));
            output.writeMessage("""
                1.View transactions
                2.Deposit
                3.Withdraw
                4.Transfer
                5.Purchase
                6.View account info
                7.Exit
                """);

            output.writeMessage("Input: ");
            String choice = input.readLine();

            if (choice.equals("7") || choice.equalsIgnoreCase("exit")
                    || choice.equalsIgnoreCase("7.exit")) {
                output.writeMessage("Exiting...\n");
                break;
            }

            if (choice.equalsIgnoreCase("1") || choice.equalsIgnoreCase("view transactions")
                    || choice.equalsIgnoreCase("1.view transactions")) {
                printUserTransactions(currentBankAccount);
                output.writeMessage("Press enter to continue.\n");
                input.readLine();
            } else if (choice.equalsIgnoreCase("2") || choice.equalsIgnoreCase("deposit")
                    || choice.equalsIgnoreCase("2.deposit")) {
                makeDeposit(currentBankAccount);
            } else if (choice.equalsIgnoreCase("3") || choice.equalsIgnoreCase("withdraw")
                    || choice.equalsIgnoreCase("3.withdraw")) {
                makeWithdrawal(currentBankAccount);
            } else if (choice.equalsIgnoreCase("4") || choice.equalsIgnoreCase("transfer")
                    || choice.equalsIgnoreCase("4.transfer")) {
                makeTransfer(currentBankAccount);
            } else if (choice.equalsIgnoreCase("5") || choice.equalsIgnoreCase("purchase")
                    || choice.equalsIgnoreCase("5.purchase")) {
                makePurchase(currentBankAccount);
            } else if (choice.equalsIgnoreCase("6") || choice.equalsIgnoreCase("view account info")
                    || choice.equalsIgnoreCase("6.view account info")) {
                printAccountInfo(currentBankAccount);
            } else {
                output.writeMessage("Invalid choice!\n");
            }
        }
    }


    public static void main(String[] args) {
        if(args.length > 0) {
            switch (args[0]) {

                case "-cu":
                    // Create User
                    if(args.length == 3) {
                        Main.createUser(args[1], args[2], false);
                    }
                    else {
                        Main.output.writeMessage("-cu needs exactly 2 arguments! Name and password.");
                    }
                    break;

                case "-ca":
                    // Create Admin
                    if(args.length == 3) {
                        Main.createUser(args[1], args[2], true);
                    }
                    else {
                        Main.output.writeMessage("-ca needs exactly 2 arguments! Name and password.");
                    }
                    break;

                case "-ea":
                    // Enter Account
                    if(args.length == 3) {
                        Main.enterAccount(args[1], args[2]);
                    }
                    else {
                        Main.output.writeMessage("-ea needs exactly 2 arguments! Name and password.");
                    }
                    break;

                case "-vi":
                    // View Info
                    if(args.length == 3) {
                        Main.viewInfo(args[1], args[2]);
                    }
                    else {
                        Main.output.writeMessage("-vi needs exactly 2 arguments! Name and password.");
                    }
                    break;

                default:
                    Main.output.writeMessage(args[1] + " unknown command");
                    Main.printUsage();
                    break;
            }
        }
        else {
            Main.printUsage();
        }
    }
}