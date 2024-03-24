package edu.cooper.ece465.zk.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** @author "Bikas Katwal" 26/03/19 */
@Getter
@AllArgsConstructor
public class File {

    private int id;
    private String name;
    private String directory;
}