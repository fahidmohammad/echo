package com.example.tpz.echo;

import java.util.ArrayList;

/**
 * Created by Dell on 07/11/2015.
 */
public class Result {
    ArrayList<String> name;
    ArrayList<String> context_id;
    ArrayList<String> context_action;
    String original;

    public Result(){
        name = new ArrayList<String>();
        context_id = new ArrayList<String>();
        context_action = new ArrayList<String>();
        original = "";
    }
}
