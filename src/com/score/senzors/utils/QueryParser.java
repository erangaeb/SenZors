package com.score.senzors.utils;

import com.score.senzors.exceptions.InvalidQueryException;
import com.score.senzors.pojos.Query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Query parser for android
 * Parse incoming queries and generate Query object
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class QueryParser {

    // define query commands
    // we support only for 4 commands
    private static List<String> commandList = Arrays.asList("LOGIN", "GET", "SHARE", "DATA", "STATUS");

    /**
     * Parser message and get query object
     * @param message message to be passed
     * @return Query object
     */
    public static Query parse(String message) throws InvalidQueryException {
        // to hold query attributes
        String command = "LOGIN";
        String user = "";
        HashMap<String, String> parameterMap = new HashMap<String, String>();

        // split message and put in linked list
        // we need to user pop operation here
        String[] tokens = message.split(" ");

        // parse query
        for (int i=0; i<tokens.length; i++) {
            String token = tokens[i];

            if(commandList.contains(token)) {
                // this is a command
                command = token;
            } else if (token.startsWith("@")) {
                // user comes with @
                // this is a user
                // get user without @ sing
                user = token.substring(1, token.length());
            } else if(token.startsWith("#")) {
                // Query parameters comes with #
                // need to store in a HashMap
                if(command.equalsIgnoreCase("LOGIN") || command.equalsIgnoreCase("DATA") || command.equalsIgnoreCase("STATUS")) {
                    // different scenario for LOGIN, DATA and STATUS
                    // LOGIN and DATA queries comes with parameters, some examples are here
                    //  1. LOGIN #username test #password test
                    //  2. DATA #gps colombo
                    //  3. STATUS #login success
                    // we remove # sing and store in the map
                    String param = token.substring(1, token.length());
                    parameterMap.put(param, tokens[i+1]);

                    // jump to two position forward
                    // we already used next token as param value
                    i++;
                } else {
                    // we remove # sing and store in the map
                    String paramValue = token.substring(1, token.length());
                    parameterMap.put("param", paramValue);
                }
            } else {
                // comes here means invalid query
                throw new InvalidQueryException();
            }
        }

        // new Query
        return new Query(command, user, parameterMap);
    }

    /**
     * Generate query sting back from Query
     * @param query Query object
     * @return query string
     */
    public static String getMessage(Query query) {
        String message = query.getCommand();

        if(!query.getUser().equals(""))
            // add user if available
            message = message.concat(" ").concat("@").concat(query.getUser());

        for(String key : query.getParams().keySet()) {
            // iterate over parameter map and add to query
            if (key.equalsIgnoreCase("param")) {
                // GET or SHARE query
                message = message.concat(" ").concat("#").concat(query.getParams().get(key));
            } else {
                // LOGIN or DATA query
                message = message.concat(" ").concat("#").concat(key).concat(" ").concat(query.getParams().get(key));
            }
        }

        return message;
    }

    /*public static void main(String[] args) {
        try {
            System.out.println(QueryParser.getMessage(QueryParser.parse("LOGIN #username test #password pass")));
            System.out.println(QueryParser.getMessage(QueryParser.parse("GET #gp @user1")));
            System.out.println(QueryParser.getMessage(QueryParser.parse("SHARE #gps @user1")));
            System.out.println(QueryParser.getMessage(QueryParser.parse("DATA #gps Colombo @user1")));
            System.out.println(QueryParser.getMessage(QueryParser.parse("LOGIN #status success")));
            System.out.println(QueryParser.getMessage(QueryParser.parse("STATUS #share success")));
            System.out.println(QueryParser.getMessage(QueryParser.parse("STATUS #login fail")));
            System.out.println(QueryParser.getMessage(QueryParser.parse("DATA #lat 345345 #lon 34545 @user2")));
        } catch (InvalidQueryException e) {
            System.out.println(e.toString());
        }
    }*/
}
