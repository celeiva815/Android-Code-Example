package cl.magnet.puntotrip.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

import cl.magnet.puntotrip.PuntoTripDB;

/**
 * Created by Tito_Leiva on 18-08-15.
 */

@Table(databaseName = PuntoTripDB.NAME)
public class Company extends BaseModel {

    @Column(name = "id")
    @PrimaryKey(autoincrement = true)
    private long id;

    @Column(name = "server_id")
    private int serverId;

    @Column(name = "name")
    private String name;

    public Company() {
    }

    public Company(String name) {
        this.name = name;
    }

    public Company(int serverId, String name) {
        this.serverId = serverId;
        this.name = name;
    }

    public static List<Company> getAll() {

        return new Select().from(Company.class).queryList();
    }

    public static Company getSingle(long companyId) {

        Condition condition = Condition.column(Company$Table.ID).eq(companyId);

        return new Select().from(Company.class).where(condition).querySingle();
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
