package sx.integracion

import groovy.sql.Sql

import javax.sql.DataSource
import java.sql.ResultSet
import java.sql.SQLException

class DataAcessObject implements  Closeable {

    private static final int COLUMN_NAME_RS_INDEX = 4

    private final Sql sql

    List<String> columns

    private String tableName

    String whereClause

    boolean connected

    boolean updateTarget = true

    DataAcessObject(final String connectionString, final String user, final String password) throws SQLException {
        this.sql = Sql.newInstance(connectionString, user, password, 'com.mysql.jdbc.Driver')
    }

    DataAcessObject( DataSource ds){
        this.sql = new Sql(ds.getConnection())
    }

    /**
     * Connects to a database table and prepares this object for storing records.
     *
     * @param tableName the name of the target database table
     *
     * @throws SQLException if a database exception occurs
     */
    void connect(final String tableName) throws SQLException {
        assert !connected, 'Already connected to a table'

        this.tableName = tableName
        final def metadata = sql.connection.metaData

        columns = metadata.getColumns(null, null, tableName, null).with {
            getColumnNames it
        }

        whereClause = metadata.getPrimaryKeys(null, null, tableName).with {
            getColumnNames it
        }.collect { final String pk ->
            "$pk = :$pk"
        }.join(' and ')

        connected = true

    }

    /**
     * Inserts a new record into the target table, or updates an existing record if a previous record with matching
     * primary key values exists.
     *
     * @param record the record to insert or update
     *
     * @throws SQLException if a database exception occurs
     */
    void storeRecord(final Map<String, Object> record) throws SQLException {
        assert connected, 'Not connected to a table'

        final def select = """
          select ${columns.join(', ')}
            from $tableName
           where $whereClause
          """

        final Map<String, Object> row = sql.firstRow(select, record)

        if (row && updateTarget) {
            updateRow row, record
        }
        else {
            println 'Faltante: ' + record.id
            insertRow record
        }
    }

    def updateRow = { final Map<String, Object> row, final Map<String, Object> record ->
        assert connected, 'Not connected to a table'

        //println "Found matching record: $record"
        final List<String> updates = row.entrySet().findAll {
            it.value != record[it.key]
        }.collect {
            // println "Updating $it.key to ${record[it.key]} from $it.value"
            it.key
        }

        if (updates) {
            final def setClause = updates.collect {
                "$it = :$it"
            }.join(', ')

            final def update = "update $tableName set $setClause where $whereClause"

            sql.executeUpdate(update, record)
        }
        else {
            //println 'No differences found. Record is dropped.'
        }
    }

    def insertRow = { final Map<String, Object> record ->
        assert connected, 'Not connected to a table'

        final def insert = """
          insert into $tableName
           (${columns.join(', ')})
          values
           (${columns.collect { ":$it" }.join(', ')})
        """
        // println "Inserting record: $record"
        sql.executeInsert(insert, record)
    }

    def getResultSetString = { final ResultSet rs, final int col ->
        final def result = []
        while (rs.next()) {
            result << rs.getString(col)
        }
        result
    }

    def getColumnNames = getResultSetString.rcurry(COLUMN_NAME_RS_INDEX)

    @Override
    void close() throws IOException {
        assert connected, 'Not connected to a table'
        sql.close()
        connected = false

    }
}
