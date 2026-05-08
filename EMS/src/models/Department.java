package models;

/**
 * Model representing a row in the `departments` table.
 */
public class Department {

    private int    id;
    private String name;

    public Department() {}

    public Department(int id, String name) {
        this.id   = id;
        this.name = name;
    }

    public int    getId()          { return id; }
    public void   setId(int id)    { this.id = id; }

    public String getName()        { return name; }
    public void   setName(String n){ this.name = n; }

    /** Used by JComboBox to display the department name. */
    @Override
    public String toString() { return name; }
}