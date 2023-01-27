import Entities.Booking;
import Entities.BookingDates;
import Entities.User;
import Entities.UserToken;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static io.restassured.config.LogConfig.logConfig;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookingTests {
    public static Faker faker;
    private static RequestSpecification request;
    private static Booking booking;
    private static BookingDates bookingDates;
    private static User user;
    private static UserToken usertoken;




    @BeforeAll
    public static void Setup(){
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        faker = new Faker();
        user = new User(faker.name().username(),
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().safeEmailAddress(),
                faker.internet().password(8,10),
                faker.phoneNumber().toString());

        bookingDates = new BookingDates("2018-01-01","2018-01-01");
        booking = new Booking(user.getFirstname(),user.getLastname(),
                (float)faker.number().randomDouble(2, 60, 100000),
                true, bookingDates,
                "");

        usertoken = new UserToken("admin","password123");


        RestAssured.filters(new RequestLoggingFilter(),new ResponseLoggingFilter(), new ErrorLoggingFilter());
    }

    @BeforeEach
    void setRequest(){
        request = given().config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                .contentType(ContentType.JSON)
                .auth().basic("admin","password123");
    }

    @Test
    @Order(1)
    public void postCreateToken_returnOK(){
        request
                .when()
                .body(usertoken)
                .post("/auth")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON).and().time(lessThan(3000L));

    }
    @Test
    @Order(2)
    public void getAllBookingsById_returnOK(){
            Response response = request
                .when()
                    .get("/booking")
                .then()
                    .extract()
                    .response();
        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    @Order(3)
    public void getAllBookingByUserFirstName_BookingExists_returnOK(){
            request
                    .when()
                        .queryParam("firstName", "Suelen")
                        .get("/booking")
                    .then()
                        .assertThat()
                        .statusCode(200)
                        .contentType(ContentType.JSON)
                    .and()
                    .body("results", hasSize(greaterThan(0)));
    }

    @Test
    @Order(4)
    public void getAllBookingsById_BookingExists_returnOK(){

            Response response = request
                .when()
                .get("/booking/8")
                .then()
                .extract()
                .response();
            Assertions.assertNotNull(response);
            Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    @Order(5)
    public void postCreateBooking_WithValidDate_returnOk(){
        Booking test = booking;
        request
                .when()
                .body(booking)
                .post("/booking")
                .then()
                .body(matchesJsonSchemaInClasspath("createBookingRequestSchema.json"))
                .and()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON).and().time(lessThan(3000L));
    }

    @Test
    @Order(6)
    public void putUpdateBooking_WithValidDate_returnOk(){
        request
                .header("Authorization", "Basic YWRtaW46cGFzc3dvcmQxMjM=")
                .when()
                .body(booking)
                .put("/booking/85")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON).and().time(lessThan(3000L));
    }

    @Test
    @Order(7)
    public void patchPartialUpdateBooking_WithValidDate_returnOk(){
        user = new User( faker.name().firstName(),
                faker.name().lastName(),"James","Brown");
        request
                .header("Authorization", "Basic YWRtaW46cGFzc3dvcmQxMjM=")
                .when()
                .body(booking)
                .put("/booking/85")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON).and().time(lessThan(3000L));
    }

    @Test
    @Order(8)
    public void deleteBooking_WithValidDate_returnOk(){

        request
                .header("Authorization", "Basic YWRtaW46cGFzc3dvcmQxMjM=")
                .when()
                .body(booking)
                .delete("/booking/178")
                .then()
                .assertThat()
                .statusCode(201)
                .and().time(lessThan(3000L));
    }

    @Test
    @Order(9)
    public void getPingHealthCheck_returnOK(){
        Response response = request
                .when()
                .get("/ping")
                .then()
                .statusLine(containsString("Created"))
                .extract().response();
        Assertions.assertNotNull(response);
        Assertions.assertEquals(201, response.statusCode());
    }

}
