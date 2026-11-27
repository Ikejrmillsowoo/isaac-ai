package com.isaacai.tools;

import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskActionsIntegrationTest {
 @Autowired TaskActions actions;
 @Autowired JdbcTemplate jdbc;
 UUID a,b;
 @BeforeEach void setup() {
  a=UUID.randomUUID(); b=UUID.randomUUID();
  for(UUID id: new UUID[]{a,b}) jdbc.update("INSERT INTO workspaces(id,name,created_at,updated_at) VALUES (?,?,now(),now())",id,id.toString());
 }
 @AfterEach void cleanup() { jdbc.update("DELETE FROM workspaces WHERE id IN (?,?)",a,b); }
 @Test void approvalAndIsolation() {
  assertFalse(actions.enabled(a));
  assertThrows(ResponseStatusException.class,()->actions.propose(a,"create_task","Review"));
  actions.setEnabled(a,true); actions.setEnabled(b,true);
  UUID id=actions.propose(a,"create_task","Review");
  assertTrue(actions.tasks(a).isEmpty());
  assertTrue(actions.history(b).isEmpty());
  assertThrows(ResponseStatusException.class,()->actions.decide(b,id,true));
  actions.decide(a,id,true);
  assertEquals(1,actions.tasks(a).size());
  assertTrue(actions.tasks(b).isEmpty());
  assertThrows(ResponseStatusException.class,()->actions.decide(a,id,true));
  assertEquals(1,actions.tasks(a).size());
  String task=actions.tasks(a).getFirst().get("id").toString();
  assertThrows(ResponseStatusException.class,()->actions.propose(b,"complete_task",task));
  UUID completion=actions.propose(a,"complete_task",task);
  actions.decide(a,completion,true);
  assertEquals(true,actions.tasks(a).getFirst().get("completed"));
 }
 @Test void rejectionAndRevocation() {
  actions.setEnabled(a,true);
  UUID id=actions.propose(a,"create_task","Rejected");
  actions.setEnabled(a,false);
  assertThrows(ResponseStatusException.class,()->actions.decide(a,id,true));
  actions.decide(a,id,false);
  assertTrue(actions.tasks(a).isEmpty());
  assertEquals("REJECTED",actions.history(a).getFirst().get("status"));
 }
}
