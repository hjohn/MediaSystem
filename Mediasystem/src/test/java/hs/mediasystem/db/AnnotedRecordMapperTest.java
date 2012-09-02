package hs.mediasystem.db;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AnnotedRecordMapperTest {
  @Mock private Database.Transaction transaction;
  @Mock private Database database;

  private AnnotatedRecordMapper<TestEmployee> employeeMapper;
  private AnnotatedRecordMapper<TestCar> carMapper;
  private AnnotatedRecordMapper<TestOption> optionMapper;

  private TestEmployee testEmployee;
  private TestEmployer testEmployer;
  private TestCar testCar;
  private TestOption testOption;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);

    employeeMapper = AnnotatedRecordMapper.create(TestEmployee.class);
    carMapper = AnnotatedRecordMapper.create(TestCar.class);
    optionMapper = AnnotatedRecordMapper.create(TestOption.class);

    testEmployee = new TestEmployee();
    testEmployer = new TestEmployer();
    testCar = new TestCar();
    testOption = new TestOption();

    when(transaction.getDatabase()).thenReturn(database);
    when(database.beginTransaction()).thenReturn(transaction);
  }

  @Test
  public void shouldApplyRelationUsingStub() {
    employeeMapper.applyValues(transaction, testEmployee, new HashMap<String, Object>() {{
      put("id", 2);
      put("name", "John Doe");
      put("employers_id", 101);
    }});

    assertEquals(new Integer(2), testEmployee.getId());
    assertEquals("John Doe", testEmployee.getName());
    assertEquals(TestEmployer.class, testEmployee.getEmployer().getClass());
  }

  @Test
  public void shouldApplyRelationUsingAssociatedObject() {
    testEmployer.setId(102);

    when(transaction.findAssociatedObject(TestEmployer.class, new Object[] {102})).thenReturn(testEmployer);

    employeeMapper.applyValues(transaction, testEmployee, new HashMap<String, Object>() {{
      put("id", 2);
      put("name", "John Doe");
      put("employers_id", 102);
    }});

    assertEquals(new Integer(2), testEmployee.getId());
    assertEquals("John Doe", testEmployee.getName());
    assertEquals(testEmployer, testEmployee.getEmployer());
  }

  @Test
  public void shouldApplyMultiFieldId() {
    carMapper.applyValues(transaction, testCar, new HashMap<String, Object>() {{
      put("countryCode", "NL");
      put("licensePlate", "01-AA-01");
      put("brand", "Australian Martini");
      put("model", "1");
    }});

    assertEquals(new TestPlate("NL", "01-AA-01"), testCar.getId());
    assertEquals("Australian Martini", testCar.getBrand());
    assertEquals("1", testCar.getModel());
  }

  @Test
  public void shouldApplyMultiFieldRelation() {
    testCar.setId(new TestPlate("NL", "01-AA-02"));

    when(transaction.findAssociatedObject(TestCar.class, new Object[] {"NL", "01-AA-02"})).thenReturn(testCar);

    optionMapper.applyValues(transaction, testOption, new HashMap<String, Object>() {{
      put("id", 1);
      put("car_cc", "NL");
      put("car_plate", "01-AA-02");
      put("optiondescription", "Mirror Dice");
    }});

    assertEquals(testCar, testOption.getCar());
    assertEquals(new Integer(1), testOption.getId());
    assertEquals("Mirror Dice", testOption.getOptionDescription());
  }

  @Test
  public void shouldFetchStub() {
    when(transaction.selectUnique(TestEmployee.class, "id=?", 201)).thenReturn(testEmployee);

    carMapper.applyValues(transaction, testCar, new HashMap<String, Object>() {{
      put("countryCode", "NL");
      put("licensePlate", "01-AA-01");
      put("brand", "Australian Martini");
      put("model", "1");
      put("owners_id", 201);
    }});

    assertEquals(new TestPlate("NL", "01-AA-01"), testCar.getId());
    assertEquals("Australian Martini", testCar.getBrand());
    assertEquals("1", testCar.getModel());
    assertEquals(testEmployee, testCar.getOwner());
  }
}
