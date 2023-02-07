# Entities without lombok

If you don't work with Lombok, here are the same entities from the unit-tests from en2do, but without using Lombok annotations.

### Customer.java

```java
@TTLIndex(value = "createTime", ttl = 10) // en2do - Expires 10 seconds after create date
@TTLIndex(value = "expireTime") //  en2do - Expires on "expireDate" clock time
public class Customer {

    // private fields
    @Id // en2do
    private UUID uniqueId;

    private int customerId;
    private String firstName;
    private String lastName;
    private String birthday;
    private String street;
    private int houseNumber;
    private Integer postalCode;
    private String city;
    private Long phoneNumber;
    private double balance;
    private double balanceRenamed;
    private List<Order> orders; // Embedded object list
    private CustomerType customerType; // enum type
    private Date createTime; // 1. ttl object
    private Date expireTime; // 2. ttl object

    // public no args constructor
    public Customer() {
    }

    // getters + setters
    public UUID getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public int getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(int houseNumber) {
        this.houseNumber = houseNumber;
    }

    public Integer getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(Integer postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getBalanceRenamed() {
        return balanceRenamed;
    }

    public void setBalanceRenamed(double balanceRenamed) {
        this.balanceRenamed = balanceRenamed;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public void setCustomerType(CustomerType customerType) {
        this.customerType = customerType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }
}
```

### Alien.java

```java
public class Alien {

    @Id // en2do
    private UUID uniqueId;

    private Map<Long, String> ufoIdList;
    private Map<Planet, Long> planetTimeMap;
    private Map<String, Planet> translationPlanetMap;

    public Alien() {
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Map<Long, String> getUfoIdList() {
        return ufoIdList;
    }

    public void setUfoIdList(Map<Long, String> ufoIdList) {
        this.ufoIdList = ufoIdList;
    }

    public Map<Planet, Long> getPlanetTimeMap() {
        return planetTimeMap;
    }

    public void setPlanetTimeMap(Map<Planet, Long> planetTimeMap) {
        this.planetTimeMap = planetTimeMap;
    }

    public Map<String, Planet> getTranslationPlanetMap() {
        return translationPlanetMap;
    }

    public void setTranslationPlanetMap(Map<String, Planet> translationPlanetMap) {
        this.translationPlanetMap = translationPlanetMap;
    }
}

```
