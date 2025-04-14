package googol;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Client{
    public static void main (String [] args) throws NotBoundException, IOException{
        Gateway_int gateway = (Gateway_int) LocateRegistry.getRegistry(args[0], Integer.parseInt(args[1])).lookup("googol");

        InputStreamReader in = new InputStreamReader (System.in);
        BufferedReader reader = new BufferedReader (in);

        System.out.println("Server ready. Waiting for input...");
        try {
            Set <String> urls = new HashSet<>();
            urls.add ("https://pt.wikipedia.org/wiki/");
            //gateway.putUrl(urls);

        } catch (Exception e) {
            e.printStackTrace(); 
        }
        
        boolean running = true;
        int num = 0;

        while (running) {
            menu();
            num = verifyInput(reader.readLine());
            System.out.println ();

            switch (num) {
                case 1:
                    search(gateway, reader);
                    break;

                case 2:
                    insertUrl(gateway, reader);
                    break;

                case 3:
                    pagesContainUrl(gateway, reader);
                    break;

                case 4:
                    running = false;
                    System.out.println("Exiting...");
                    break;
            }
        }

    }

    public static void menu () {
        System.out.println ();
        System.out.println ("Choose what you want to do: ");
        System.out.println ("1 - Search");
        System.out.println ("2 - Add a new URL to process");
        System.out.println ("3 - Discover pages that contain Url");
        System.out.println ("4 - Exit");
    }

    public static int verifyInput (String input){
        boolean invalid = true;
        int num = 0;
        boolean valid = false;

        while (!valid) {
            
            try {
                num = Integer.parseInt(input);
                if (num >= 1 && num <= 4) {
                    valid = true;
                } else {
                    System.out.println("Invalid input. Please enter a number between 1 and 4.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
            
        }

        return num;
    }

    public static boolean search (Gateway_int gateway, BufferedReader reader){
        boolean stop = false;
        try {
            while (!stop){
                System.out.print("Search: ");
                String input = reader.readLine();
                String[] line = input.trim().split("\\s+");
    
                if (line.length == 0 || line[0].isBlank()) {
                    System.out.println("Empty input, please enter a valid word!");
                    break;
                }
    
                List<String> results = gateway.search(line);
    
                if (results == null || results.isEmpty()) {
                    System.out.println();
                    System.out.println("No results found");
                    System.out.println();
                    System.out.print("Continue to search? (y/n): ");

                    String answer = reader.readLine();
                    System.out.println ();

                    if (answer.equals("n")) {
                        stop = true;
                        System.out.println ("Going back to the main menu...");

                    } else if (answer.equals("y")) {
                        stop = false;
                        continue;

                    } else {
                        System.out.println("Invalid input, please enter 'y' or 'n'!");
                        continue;
                    }
                }

                else {
                    System.out.println();
                    System.out.println("Results found for " + String.join(" ", line));
                    for (String url : results) {
                        System.out.println(url);
                    }
                    System.out.println();
                    System.out.print("Continue to search? (y/n): ");

                    String answer = reader.readLine();
                    System.out.println ();
                    if (answer.equals("n")) {
                        stop = true;
                        System.out.println ("Going back to the main menu...");
                    } else if (answer.equals("y")) {
                        stop = false;
                        continue;
                    } else {
                        System.out.println("Invalid input, please enter 'y' or 'n'!");
                        continue;
                    }
                }
            }
                
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stop;
    }

    public static void insertUrl (Gateway_int gateway, BufferedReader reader) {
        boolean stop = false;
        try{
            while (!stop){
                System.out.print("Insert Url: ");
                String url1 = reader.readLine();
                Set<String> newUrls = new HashSet<>();
                newUrls.add(url1);
                gateway.putUrl(newUrls);
                System.out.println();

                System.out.print("Want to insert more urls? (y/n): ");
                String answer = reader.readLine();
                System.out.println ();
                
                if (answer.equals("n")) {
                    stop = true;
                    System.out.println ("Going back to the main menu...");
                } else if (answer.equals("y")) {
                    stop = false;
                    continue;
                } else {
                    System.out.println("Invalid input, please enter 'y' or 'n'!");
                    continue;
                }

            }

        }  catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void pagesContainUrl (Gateway_int gateway, BufferedReader reader) {
        boolean stop = false;
        try {
            while (!stop){
                System.out.print("Insert Url: ");
                String url2 = reader.readLine();

                if (gateway.getReachableUrls(url2) == null || gateway.getReachableUrls(url2).isEmpty()){
                    System.out.println();
                    System.out.println ("The url is not available in any page...");
                }
                
                else {
                    System.out.println();
                    System.out.println("The inserted url is available in: ");
                    System.out.println();
        
                    for (String url : gateway.getReachableUrls(url2)) {
                        System.out.println(url);
                    }
                    System.out.println();
                }
    
                System.out.print("Want to continue? (y/n): ");
                String answer = reader.readLine();
                System.out.println ();

                if (answer.equals("n")) {
                    stop = true;
                    System.out.println ("Going back to the main menu...");
                } else if (answer.equals("y")) {
                    stop = false;
                    continue;
                } else {
                    System.out.println("Invalid input, please enter 'y' or 'n'!");
                    continue;
                }

            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}