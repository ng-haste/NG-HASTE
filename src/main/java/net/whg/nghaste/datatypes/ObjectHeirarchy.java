package net.whg.nghaste.datatypes;

import java.util.ArrayList;
import java.util.List;
import net.whg.nghaste.IDataInstance;
import net.whg.nghaste.IDataType;
import net.whg.nghaste.IFunction;

/**
 * This class is a utility class which allows new data types to be created
 * dynamically by a name without worrying about creating a new class for each.
 * Data types created through this class have support for heirarchies, allowing
 * them to automatically be casted up and down in the output function.
 * <p>
 * Note: To enable casting down, the down cast functions must be added to the
 * environment before running it. See {@link #createDownCasts()}
 */
public class ObjectHeirarchy
{
    /**
     * This class is a container which represents a data type which was created
     * dynamically. It holds all of the properties of a basic data type and supports
     * automatic casting up.
     */
    public static class DataObj implements IDataType
    {
        private final String name;
        private final DataObj[] parents;

        /**
         * Creates a new data object instance.
         * 
         * @param name
         *     - The name of this data object.
         * @param parents
         *     - The parents this data object extend from.
         */
        private DataObj(String name, DataObj[] parents)
        {
            this.name = name;
            this.parents = parents;
        }

        @Override
        public boolean canConnectTo(IDataType dataType)
        {
            if (dataType == this)
                return true;

            for (DataObj obj : parents)
                if (obj != null && obj.canConnectTo(dataType))
                    return true;

            return false;
        }

        /**
         * Gets the name of this data object.
         * 
         * @return The name.
         */
        public String getName()
        {
            return name;
        }

        @Override
        public String toString()
        {
            return String.format("DataObj(%s)", name);
        }
    }

    /**
     * This function is a simple pass-through function which allows once data type
     * to be casted to another data type.
     */
    public static class DowncastDataTypeFunction implements IFunction
    {
        private final IDataType[] input;
        private final IDataType[] output;

        private DowncastDataTypeFunction(IDataType input, IDataType output)
        {
            this.input = new IDataType[] {input};
            this.output = new IDataType[] {output};
        }

        @Override
        public IDataType[] getInputs()
        {
            return input;
        }

        @Override
        public IDataType[] getOutputs()
        {
            return output;
        }

        @Override
        public IDataInstance[] execute(IDataInstance[] inputs)
        {
            return inputs;
        }

    }

    private final List<DataObj> objects = new ArrayList<>();

    /**
     * Creates a new object heirarchy and initializes it with the root data type.
     */
    public ObjectHeirarchy()
    {
        addDataType("Object");
    }

    /**
     * Creates a new data type and adds it to this object heirarchy.
     * 
     * @param name
     *     - The name of the data object to create.
     * @param parents
     *     - The parents this new data type should extend from. Data types are
     *     always allowed to cast up to parent objects, recursively. This list
     *     should not contain the root object, as that is added automatically.
     * @return The newly created data type.
     * @throws IllegalArgumentException
     *     If the root object is in the parents list, or if another data object with
     *     the same name exists.
     */
    public DataObj addDataType(String name, DataObj... parents)
    {
        DataObj root = getRootObject();
        for (DataObj d : parents)
            if (d == root)
                throw new IllegalArgumentException("Parent list cannot contain the root object!");

        if (getObject(name) != null)
            throw new IllegalArgumentException("An object with the name ''" + name + "'' already exists!");

        DataObj[] p = new DataObj[parents.length + 1];
        p[0] = getRootObject();

        for (int i = 0; i < parents.length; i++)
            p[i + 1] = parents[i];

        DataObj dataObj = new DataObj(name, p);
        objects.add(dataObj);

        return dataObj;
    }

    /**
     * Generates a list of all downcast functions, and logical axioms for objects in
     * this list.
     * 
     * @return The list of functions which should be added to the environment.
     */
    public List<IFunction> createDownCasts()
    {
        List<IFunction> functions = new ArrayList<>();

        for (DataObj obj : objects)
            for (DataObj parent : obj.parents)
                functions.add(new DowncastDataTypeFunction(parent, obj));

        return functions;
    }

    /**
     * Gets the root data type. All other data types extend from this data type.
     * 
     * @return The root data type.
     */
    public DataObj getRootObject()
    {
        return objects.get(0);
    }

    /**
     * Finds and returns a data object by its name.
     * 
     * @param name
     *     - The name of the data object.
     * @return The data object with the given name, or null if the object could not
     *     be found.
     */
    public DataObj getObject(String name)
    {
        for (DataObj obj : objects)
            if (obj.getName()
                   .equals(name))
                return obj;

        return null;
    }
}