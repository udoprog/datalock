package eu.toolchain.datalock;

import com.google.common.collect.ImmutableList;
import eu.toolchain.scribe.EntityMapper;
import lombok.Data;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static eu.toolchain.datalock.Filter.keyFilter;
import static eu.toolchain.datalock.Mutation.insert;
import static eu.toolchain.datalock.Mutation.update;
import static eu.toolchain.datalock.PathElement.element;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DatalockIT {
  private final A a1 = new A(42);
  private final A a2 = new A(43);

  private CoreDatalockClient client;

  private DataLockEntityMapper mapper;
  private DataLockEncoding<A> encoding;

  private PathElement org;

  private Entity.KeyedEntity e1;
  private Entity.KeyedEntity e2;

  @Before
  public void setup() throws Exception {
    final String namespace = "it-" + UUID.randomUUID().toString().replace("-", "");

    client = CoreDatalockClient
        .builder()
        .namespaceId(namespace)
        .projectId("datastore")
        .host("localhost")
        .port(8080)
        .usePlainText(true)
        .build();

    mapper = new DataLockEntityMapper(EntityMapper.defaultBuilder().build());
    encoding = mapper.encodingFor(A.class);

    org = element("org", "a");

    e1 = encoding.encodeEntity(a1).withKey(client.key(org, element("id", "a")));
    e2 = encoding.encodeEntity(a2).withKey(client.key(org, element("id", "b")));

    client.start().join();

    client.commit(ImmutableList.of(insert(e1), insert(e2))).join();
  }

  @After
  public void teardown() throws Exception {
    client.stop().get(10L, TimeUnit.SECONDS);
  }

  @Test
  public void runQueryTest() {
    final Query q = Query.builder().filter(keyFilter(Operator.EQUAL, e1.getKey())).build();

    final RunQueryResponse results = client.runQuery(q).join();

    assertEquals(e1, results.getEntities().get(0));
  }

  @Test
  public void transactionTest() throws Exception {
    final TransactionResult result = client.transaction().thenCompose(t -> {
      return t.lookupOne(e1.getKey()).thenApply(e -> {
        final Entity.KeyedEntity entity = e.get();
        final A a = encoding.decodeEntity(entity);

        return ImmutableList.of(
            update(encoding.encodeEntity(a.withAge(a.getAge() + 1)).withKey(entity.getKey())));
      }).thenCompose(t::commit);
    }).join();

    assertTrue(result.isCommited());

    final LookupResponse results = client.lookup(ImmutableList.of(e1.getKey())).get();
    final A a = encoding.decodeEntity(results.getFound().get(0));

    assertEquals(a1.getAge() + 1, a.getAge());
  }

  @Data
  public static class A {
    @ExcludeFromIndexes
    private final int age;

    public A withAge(final int age) {
      return new A(age);
    }
  }
}
