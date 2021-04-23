# RoomExample
Android Room 라이브러리 사용법, 예제

# Room
* 구글에서 만든 공식 ORM(Object-relational mapping)
* SQLite를 내부적으로 사용하고 있어 기능적으로 동일하지만 데이터베이스를 구조적으로 분리하여 편리한 데이터 접근과 유지 보수의 유연성을 높여준다.

### 1. 구성요소
* **Database**  
    * Database 접근 지점을 제공하며 DAO를 관리한다.  
    * Annotation 내에 사용할 Entity 목록을 작성해야 한다.
* **DAO(Data Access Object)**  
    * Database에 접근하는 메소드들을 포함하며 Annotation으로 관리된다.  
    * LiveData를 이용하면 Observable query를 이용할 수 있다.  
* **Entity**  
    * 테이블을 의미
  
<img src="https://miro.medium.com/max/750/1*jT94pc71uD_A2TPN_E2ulg.png" width="400" height="400">

### 2. Database의 특징
* Database 접근 지점을 제공하며, DAO를 관리한다.
* 클래스에 `@Database` 어노테이션을 붙이며, 아래의 조건을 만족해야 한다.
    * RoomDatabase 클래스를 상속받는 추상 클래스여야 한다.
    * 어노테이션 내에 Database에 들어갈 Entity 목록을 배열로 포함해야 한다.
    * 파라미터가 0개인 추상 메소드를 포함하고 @Dao 어노테이션된 클래스를 반환한다. 이를 통해 RoomDatabase에게 관리 권한을 위임하여 직접적으로 접근하는 것을 막는다.
* Rumtime에 Room.databaseBuilder()를 호출해 데이터베이스 인스턴스를 얻을 수 있다.
* 인스턴스를 만드는 과정은 많은 비용이 들지만 접근은 자주하기 때문에 문서에서는 싱글톤 패턴을 이용해 만드는 것을 권장하고 있다.
```kotlin
@Database(entities = [UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        private val DB_NAME = "room-db"
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context)
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                    }
                }).build()
        }
    }

}
```

### 3. DAO(Data Access Object)의 특징
* Database에 접근하는데 사용되는 메소드들을 갖고 있으며, 어노테이션으로 관리된다.
* SELECT, INSERT, DELETE 등 데이터를 읽거나 쓸 때 사용한다.
* LiveData를 사용하면 Observable Query를 이용할 수 있다.
* `@DAO` Annotation을 활용하며, `interface` or `abstract class`로 작성해야 한다.
```kotlin
@Dao
interface UserDao {
    @Query("SELECT * FROM userentity")
    fun getAll(): List<UserEntity>

    @Insert
    fun insert(userEntity: UserEntity)
}
```

* **@Insert** : 데이터를 입력할 때 사용
* **@Update** : 데이터를 갱신할 때 사용, 전달받은 매개변수의 PK값에 매칭되는 entity를 찾아 갱신
* **@Delete** : 데이터를 삭제할 때 사용, 전달받은 매개변수의 PK값에 매칭되는 entity를 찾아 삭제
* **@Query** : 데이터를 선택할 때 사용 (다른 구문은 Annotation을 사용하는게 일반적)
    Query는 DAO클래스 중 가장 핵심 부분이며, 데이터를 읽고 쓸 수 있게 한다.  
    컴파일 시에 query검사가 이루어지기 때문에 런타임 오류를 최소화할 수 있다.
    * Simple Query : List 형태의 리턴값을 받으며 변화를 감지할 수 없다.
    * Observable Query : LiveData 형태의 리턴값을 받으며 데이터가 변화할 경우 변화를 감지하여 데이터를 재갱신
    ```kotlin
    //Simple Query
    @Query("SELECT * FROM USERENTITY")
    fun getAll(): List<UserEntity>

    @Query("SELECT * FROM UserEntity where custom_NAME = :name")
    fun getWithName(name: String): List<UserEntity>

    @Query("SELECT * FROM UserEntity where custom_NAME in (:names)")
    fun getWithNames(names: ArrayList<String>): List<UserEntity>

    //Observable Query
    @Query("SELECT * FROM UserEntity")
    fun getAllObservable(): LiveData<List<UserEntity>>

    @Query("SELECT * FROM UserEntity where custom_NAME = :name")
    fun getWithNameObservable(name: String): LiveData<List<UserEntity>>

    @Query("SELECT * FROM UserEntity where custom_NAME in (:names)")
    fun getWithNamesObservable(names: ArrayList<String>): LiveData<List<UserEntity>>
    ```


### 4. Entity의 특징
* Database 내의 테이블을 의미한다.
* `@Entity(tableName="테이블이름")`으로 테이블 이름을 지정할 수도 있지만, 지정하지 않을 경우 default값(=Class명)으로 클래스 이름이 지정되며 대소문자를 구분하지 않는다.
* `@PrimaryKey`로 PK값을 지정할 수 있고, 중복 시 오류가 발생한다.  
    `autoGenerate=true`를 통하여 자동으로 Key값을 생성할 수 있다.
* `@ColumnInfo`로 컬럼명을 지정할 수 있다.
```kotlin
@Entity(tableName = "userENTITY")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) var userId: Int = 0
    , @ColumnInfo(name = "custom_NAME") var name: String = ""
    , @ColumnInfo(name = "CUSTOM_address") var address: String = ""
    , @Ignore var image: Bitmap? = null
)
```

<br>

---


## Example

### 1. Entity
```kotlin
@Entity(tableName = "tb_contacts")
data class Contacts(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "tel")
    var tel: String
)
```
* @PrimaryKey 어노테이션과 autoGenerate=true를 이용해서 Contacts가 새로 생길 때마다 id를 자동으로 올려준다.

### 2. DAO
```kotlin
@Dao
interface ContactsDao {
    @Query("SELECT * FROM tb_contacts")
    fun getAll(): List<Contacts>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg contacts: Contacts)

    @Delete
    fun delete(contacts: Contacts)
```
* getAll() : 데이터를 전부 가져올 함수
* insertAll() : 데이터를 넣어줄 함수, 데이터가 여러개 필요할 수 있기 때문에 가변인자 vararg를 사용
* @Insert(onConflict = OnConflictStrategy.REPLACE)는 Insert할 때 PK가 겹치는 것이 있으면 덮어 쓴다는 의미이다.

### 3. AppDatabase
```kotlin
@Database(entities = [Contacts::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactsDao() : ContactsDao

    companion object {
        private var instance : AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context) : AppDatabase? {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database-contacts"
                ).allowMainThreadQueries().build()
            }
            return instance
        }
    }

}
```
* version은 추후 Contacts를 변경할 때 migration 할 수 있는 기준이 된다.
* 어디서든 접근 가능, 중복 생성되지 않게 싱글톤으로 companion object를 이용해서 만들어준다.

### 4. Activity
* db에 추가하고 삭제하는 코드만 작성
```kotlin
db?.contactsDao()?.insertAll(contact) // DB에 추가
db?.contactsDao()?.delete(contacts = contacts) // DB에서 삭제
```
