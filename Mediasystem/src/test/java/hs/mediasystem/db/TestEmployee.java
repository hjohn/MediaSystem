package hs.mediasystem.db;

@Table(name = "employees")
public class TestEmployee {

  @Id
  private Integer id;

  @Column
  private String name;

  @Column(name = "employers_id")
  private TestEmployer employer;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public TestEmployer getEmployer() {
    return employer;
  }

  public void setEmployer(TestEmployer employer) {
    this.employer = employer;
  }
}
