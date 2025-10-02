# Documentation

## System Architecture

- I am using the Controller-Service-Repository architectural pattern which is a form of the 3-layer-architectural pattern. The Repository layer can read and write in the database, the Controller layer can communicate with the client and the Service layer contains the business logic.

## System Interface

- Communication with the backend is provided via REST endpoints. A client can send an receive data using HTTP requests and responses, which may contain objects in JSON format. In some cases url parameters are also used.

### Authentication Endpoints

#### POST `/api/auth/login`
**Description:**  
Authenticate a user using credentials.

**Request Body:**  
```json
{
  "username": "string",
  "password": "string"
}
```

**Response:**  
- `200 OK` with user details if authentication is successful.
- `401 Unauthorized` if credentials are invalid.

---

#### DELETE `/api/auth/logout`
**Description:**  
Logs out the current user.

**Response:**  
- `200 OK` on successful logout.

---

### User Endpoints

#### POST `/api/users`
**Description:**  
Create a new user.

**Request Body:**  
JSON representation of a user:
```json
{
  "username": "string",
  "password": "string"
}
```

**Response:**  
- `200 OK` with created user details.
- `409 Conflict` if username is not unique.
- `406 Not Acceptable` if user cannot be created.

---

#### GET `/api/users/all`
**Description:**  
Retrieve all users.

**Response:**  
- `200 OK` with a list of users.

---

### Task Endpoints

#### GET `/api/tasks/{id}`
**Description:**  
Get a task by its ID.

**Response:**  
- `200 OK` with task details.
- `404 Not Found` if the task does not exist.

---

#### GET `/api/tasks/all`
**Description:**  
Get all tasks.

**Response:**  
- `200 OK` with a list of all tasks.

---

#### POST `/api/tasks/all`
**Description:**  
Get tasks by filter, sorting, and paging.

**Query Parameters:**
- `offset` (int, default: 0): Paging offset.
- `orderBy` (string, default: ""): Field to sort by (`dueDate`, `status`).
- `ascending` (boolean, default: true): Sort direction.

**Request Body:**  
JSON representation of a filter task (fields can be omitted to ignore filter):
```json
{
  "assignee": { "id": 2, "username": "assignee" },
  "creator": { "id": 1, "username": "creator" },
  "dueDate": "2025-10-05",
  "status": "NEW",
  "description": "desc"
}
```

**Response:**  
- `200 OK` with a `SearchResult` object:
  ```json
  {
    "numberOfResults": 25,
    "currentOffset": 0,
    "tasks": [ ... ]
  }
  ```

---

#### POST `/api/tasks`
**Description:**  
Create a new task.

**Request Body:**  
JSON representation of a task (creator is set automatically):
```json
{
  "description": "string",
  "status": "NEW",
  "assignee": { "id": 2, "username": "assignee" },
  "dueDate": "2025-10-05"
}
```

**Response:**  
- `200 OK` with created task details.
- `406 Not Acceptable` if task cannot be created.

---

#### PUT `/api/tasks/{id}`
**Description:**  
Update an existing task.

**Request Body:**  
JSON representation of the updated task.

**Response:**  
- `200 OK` with updated task details.
- `404 Not Found` if the task does not exist.

---

#### DELETE `/api/tasks/{id}`
**Description:**  
Delete a task by its ID.

**Response:**  
- `204 No Content` on successful deletion.
- `404 Not Found` if the task does not exist.

---

## Data Model

The task Model class:
-   description: String
-   creator: User
-   assignee: User (can be null)
-   status: Enum(NEW, IN_PROCESS, COMPLETED, CANCELLED)
-   createdAt: LocalDate
-   dueDate: LocalDate

I also created a Data Transfer Object for the User class, which does not contain the password property. This way it can be safely sent to the client.

## Persistence

I am using the embedded (in-file) H2 database, because it's a lightweight but functional enough database for this assignment. If needed, it can be replaced by any other database by modifying the `spring.datasource.url` property in `application.properties`.

When starting the application, a `CommandLineRunner` creates a default 'admin' user if the user table is empty.

## Error Handling

My custom exception classes are individually mapped to responses with a fitting error code and the exception message in the body by the`DefaultExceptionHandler` class. If any other exception occurs, a response with the code 500 (INTERNAL_SERVER_ERROR) with no body is sent.

## Key Design decisions

### Used technologies

- Spring Boot
- Spring Data JPA
- Spring Security
- Jackson Object Mapper
- H2
- Angular
- Angular Material

## AI Tools

### Copilot GPT-4.1 Agent + Ask mode

I used it generate code: I generated some controller and service methods and most of the tests on the backend and most of the components and services on the frontend. I also generated the documentation for most endpoints.

propts:

#### backend:

- In the exceptions package, create the following exceptions: UserNotFoundException, UserNameIsNotUniqueException, WrongUsernameOrPasswordException. The first two should extend an abstract UserException class. The constructors should have no arguments, and they should call super() with a fitting exception message.

- Change TaskService so that its methods never return null, but throw taskNotFOundException.

- Generate code for AuthController: it should have one rest endpoint that can be used to log in a user. Use Jackson ObjectMapper to deserialize the user object. The Credentials class should be used

- Generate UserServiceTest and TaskServiceTest: create unit tests for all public methods. Use Mockito to mock repository classes. Test case method names should start with "should".

- Refactor TaskServiceTest: move initalization of 'user' to a setUp method called before each test.

- Generate tests for the new filtering method "listTasksByFilter" in the existing TaskController Test class. Keep the old tests and make the new ones in a similar manner. prepare scenarios for testing each of the 5 filter types, and one with all of them combined. Create tests for all sorting types (ascending, descending with both allowed fields) with no filters and one test case with all filters and sorting. create tests for paging: the number of total results should be correct and only 10 elements should be returned. 

- Create integration tests for user and task related logic. No need to mock anything, just use the SpringBootTest annotation. The classes should be called TaskControllerTest and UserControllerTest.

- Create integration tests for AuthController, similar to UserControllerTests.

- Refactor AuthControllerTest, TaskControllerTest and UserControllerTest: use TestRestTemplate instead of MockMvc. Keep the testing logic intact.

#### frontend:

- Generate User, Task, Status, Credentials .ts files.

- Generate code for ApiService which uses fetch api to send get, post, update and delete http requests to an api  defined in environment.ts. It should be able to send json in the request body and handle errors. It will be used by several other, more specific services.

- Create UserService which is using ApiService to access the api.

- Create TaskService which is using ApiService to access the api.

- Create AuthService which is using ApiService to access the api.

- Generate code for the login component using Angular material. It should use AuthService. The login form must be validated: fields cannot be empty. If the login is successful, navigate to "/tasks". If not, display an error message under the login form.

- generate AppComponent using Angular Material. At the top, there should be a toolbar. If the user is not logged in, there should be a link at the right to log in. If the user is logged in, there should be a text saying "logged in as ... " and a link to log out. If navigating to the login path, the login component should appear under the toolbar. Use AuthService to determine whether the user is logged in or not.

- Generate ListTasksComponent using Angular material. It should use the TaskService's getTenTasks method. It should consist of a search form and a table to display the results. In the search form, there should be a search button and 5 fields: assignee, creator, dueDate, status and description. assignee and creator are selectors using UserService. DueDate is a datepicker field, status is a selector of Status enum and description is a basic field. They can be left empty as well. They are used to create a filter Task that is needed to call getTenTasks. below them, the results should be listed: all fields except id are shown. there should be two extra cells in each row: they should contain links to edit and delete the record. delete should be red and have a trash can icon next to it.

- Generate edit-task component using Angular Material. this component can be used to create a new task, or edit an existing one. It uses TaskService to communicate with the api. It also needs  UserService to list users for the the assignee field. The field id is not shown. creator is shown for existing tasks but cannot be edited. After successful update or creation, navigate to TaskListComponent.

- Generate code for Sign up component. The form consists of a username, a password and a confirm password field. All fields are required, the two passwords are validated. If the sign up is successful, navigate to login page. If not, display an error message under the form. Create html and css in a similar way as in login component.

- When submitting the form and the password and the confirm password don't match, an error message should be displayed.

- Create a DTO class for the User model class. It should contain all User fields except password. Use it when sending User details through Rest endpoints. Update the related test, service and controller methods.

#### documentation:

- In documentation.md, write documentation for authentication, user and task related endpoints, similar to the one I already wrote about the login endpoint.

### ChatGpt GPT-5

I used it to ask broad questions about topics I am not familiar with: like authentication with Spring Security and ways to populate the database when starting a Spring application.

prompts:

- how does authenticationManager.authenticate(Authentication authentication) work?

- Where does Spring Security store authentication data at the client?

- How to create calls with the same session using MockMvc?

- How to check set-cookie header with curl?

- How to page and sort at the same time with JPA?

- How to annotate enums in JPA if I want to store their ordinals only?

- How to configure CORS with Spring Security?

- How to populate db when starting a Spring application (using JPA)?