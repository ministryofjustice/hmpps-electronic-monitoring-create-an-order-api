package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.ScriptUtils
import java.sql.DriverManager
import java.util.UUID

class Up3SimulationMigrationTest {
  @Test
  fun `receipt migration enforces identities preserves creation recovery and cascades return data`() {
    DriverManager.getConnection("jdbc:h2:mem:up3-receipt-migration;MODE=PostgreSQL").use { connection ->
      connection.createStatement().use { it.execute("CREATE TABLE orders (id UUID PRIMARY KEY)") }
      ScriptUtils.executeSqlScript(
        connection,
        ClassPathResource("db/up3-simulation/V2__Add_Up3_Simulation_Receipts.sql"),
      )
      val orderId = UUID.randomUUID()
      val requestId = UUID.randomUUID()
      val eventId = UUID.randomUUID()
      connection.createStatement().use { sql ->
        sql.executeUpdate("INSERT INTO orders VALUES ('$orderId')")
        sql.executeUpdate(
          """
          INSERT INTO up3_simulation_return_event
          (event_id, order_id, target_order_version_id, simulated_submission_version_id,
           case_id, payload_hash, payload, created_at, status, next_publication_attempt_at)
          VALUES ('$eventId', '$orderId', '${UUID.randomUUID()}', '${UUID.randomUUID()}',
                  'test-case', '${"0".repeat(64)}', '{}', CURRENT_TIMESTAMP, 'PENDING_PUBLICATION', CURRENT_TIMESTAMP)
          """.trimIndent(),
        )
        sql.executeUpdate(
          """
          INSERT INTO up3_simulation_creation
          (creation_request_id, payload_hash, created_by, requested_state, created_at, order_id)
          VALUES ('$requestId', '${"0".repeat(64)}', 'test-user', 'DRAFT', CURRENT_TIMESTAMP, '$orderId')
          """.trimIndent(),
        )
        org.assertj.core.api.Assertions.assertThatThrownBy {
          sql.executeUpdate(
            """
            INSERT INTO up3_simulation_creation
            (creation_request_id, payload_hash, created_by, requested_state, created_at)
            VALUES ('$requestId', '${"0".repeat(64)}', 'test-user', 'DRAFT', CURRENT_TIMESTAMP)
            """.trimIndent(),
          )
        }.isInstanceOf(java.sql.SQLException::class.java)
        sql.executeQuery("SELECT publication_attempts, processing_failures FROM up3_simulation_return_event").use {
          assertThat(it.next()).isTrue()
          assertThat(it.getInt(1)).isZero()
          assertThat(it.getInt(2)).isZero()
        }
        sql.executeUpdate("DELETE FROM orders")
        sql.executeQuery("SELECT COUNT(*) FROM up3_simulation_return_event").use {
          it.next()
          assertThat(it.getInt(1)).isZero()
        }
        sql.executeQuery("SELECT order_id FROM up3_simulation_creation WHERE creation_request_id='$requestId'").use {
          assertThat(it.next()).isTrue()
          assertThat(it.getObject(1, UUID::class.java)).isEqualTo(orderId)
        }
      }
    }
  }

  @Test
  fun `migration creates simulation storage and removes it when its order is deleted`() {
    DriverManager.getConnection("jdbc:h2:mem:up3-migration;MODE=PostgreSQL").use { connection ->
      connection.createStatement().use { it.execute("CREATE TABLE orders (id UUID PRIMARY KEY)") }
      ScriptUtils.executeSqlScript(connection, ClassPathResource("db/up3-simulation/V1__Add_Up3_Simulation.sql"))
      val orderId = UUID.randomUUID()
      connection.prepareStatement("INSERT INTO orders (id) VALUES (?)").use {
        it.setObject(1, orderId)
        it.executeUpdate()
      }
      connection.prepareStatement(
        """
        INSERT INTO up3_simulation (order_id, version_id, case_id, case_state, enrolled_by, enrolled_at)
        VALUES (?, ?, 'test-case', 'NEW', 'test-user', CURRENT_TIMESTAMP)
        """.trimIndent(),
      ).use {
        it.setObject(1, orderId)
        it.setObject(2, UUID.randomUUID())
        it.executeUpdate()
      }
      connection.createStatement().use {
        it.executeQuery("SELECT captured_user_emails, captured_no_emails FROM up3_simulation").use { result ->
          assertThat(result.next()).isTrue()
          assertThat(result.getInt(1)).isZero()
          assertThat(result.getInt(2)).isZero()
        }
        it.executeUpdate("DELETE FROM orders")
        it.executeQuery("SELECT COUNT(*) FROM up3_simulation").use { result ->
          result.next()
          assertThat(result.getInt(1)).isZero()
        }
      }
    }
  }
}
