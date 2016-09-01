package eu.toolchain.datalock;

import com.google.common.collect.ImmutableList;
import eu.toolchain.scribe.EntityMapper;
import lombok.Data;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static eu.toolchain.datalock.Filter.ancestorFilter;
import static eu.toolchain.datalock.Filter.compositeFilter;
import static eu.toolchain.datalock.Filter.keyFilter;
import static eu.toolchain.datalock.Mutation.insert;
import static eu.toolchain.datalock.Mutation.update;
import static eu.toolchain.datalock.PathElement.element;
import static org.junit.Assert.assertEquals;

public class DataLockIT {
  private final A a1 = new A(42);
  private final A a2 = new A(43);

  private DataLockClientImpl client;

  private DataLockEntityMapper mapper;
  private DataLockEncoding<A> encoding;

  private PathElement org;

  private KeyedEntity e1;
  private KeyedEntity e2;

  @Before
  public void setup() throws Exception {
    final String namespace = "it-" + UUID.randomUUID().toString().replace("-", "");

    client = DataLockClientImpl.builder().namespace(namespace).projectId("steel-ridge-91615")
                               // .credential(credential)
                               .url("http://localhost:8080").build();

    mapper = new DataLockEntityMapper(EntityMapper.defaultBuilder().build());
    encoding = mapper.encodingFor(A.class);

    org = element("org", "a");

    e1 = encoding.encodeEntity(a1).withKey(client.key(org, element("id", "a")));
    e2 = encoding.encodeEntity(a2).withKey(client.key(org, element("id", "b")));

    client.start().join();

    client.commit(ImmutableList.of(insert(e1), insert(e2))).get();
  }

  @After
  public void teardown() throws Exception {
    client.stop().get();
  }

  @Test
  public void runQueryTest() throws Exception {
    final Query q = Query.builder().filter(keyFilter(Operator.EQUAL, e1.getKey())).build();

    final RunQueryResponse results = client.runQuery(q).get();

    assertEquals(e1, results.getEntities().get(0));
  }

  @Test
  public void transactionTest() throws Exception {
    final Query q = Query
        .builder()
        .filter(compositeFilter(ancestorFilter(client.key(org)),
            keyFilter(Operator.EQUAL, e1.getKey())))
        .build();

    client
        .transaction()
        .thenCompose(t -> t
            .runQuery(q)
            .thenCombine(t.lookup(ImmutableList.of(e1.getKey())), (results, entities) -> {
              final KeyedEntity entity = results.getEntities().get(0);

              final A a = encoding.decodeEntity(entity);

              return ImmutableList.of(update(
                  encoding.encodeEntity(a.withAge(a.getAge() + 1)).withKey(entity.getKey())));
            })
            .thenCompose(t::commit))
        .get();

    final RunQueryResponse results = client.runQuery(q).get();
    final A a = encoding.decodeEntity(results.getEntities().get(0));

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
