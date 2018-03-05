/**
 * MIT License
 *
 * Copyright (c) 2017 Wreulicke
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.wreulicke.simple.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.wreulicke.simple.TestDatabaseConfiguration;

@RunWith(SpringRunner.class)
@Import(TestDatabaseConfiguration.class)
@DataJpaTest
public class UserRepositoryTest {

  @Autowired
  UserRepository repository;

  @Test
  public void testSave() {
    User user = new User();
    user.setUsername("test");
    user.setPassword("test");
    User actual = repository.save(user);
    assertThat(actual.getId()).isNotNull();
  }

  @Test
  public void testFindByUsername() {
    User user = new User();
    user.setUsername("test");
    user.setPassword("test");
    repository.save(user);
    assertThat(repository.findByUsername("test")).isNotEmpty()
      .map(User::getUsername)
      .hasValue("test");
  }


  @Test
  public void testUpdate() {
    User user = new User();
    user.setUsername("test");
    user.setPassword("test");
    User actual = repository.save(user);

    actual.setPassword("other");

    repository.save(actual);
    assertThat(actual.getPassword()).isEqualTo("other");
  }


  @Test
  public void testDelete() {
    User user = new User();
    user.setUsername("test");
    user.setPassword("test");
    user = repository.save(user);

    repository.delete(user);

    assertThat(repository.findOne(user.getId())).isNull();
    assertThat(repository.findByUsername(user.getUsername())).isEmpty();
  }


}
