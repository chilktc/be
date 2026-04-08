package be.greenroom.graph.repository.dao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import be.greenroom.graph.dto.response.GraphEdgeResponse;
import be.greenroom.graph.dto.response.GraphNodeResponse;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrganizationGraphQueryDao {

	private final JdbcTemplate jdbcTemplate;

	public List<GraphNodeResponse> findOrganizationNodes() {
		return jdbcTemplate.query(
			"""
				SELECT
				    gn.label,
				    gn.grp,
				    AVG(gn.weight) AS weight,
				    SUM(gn.mention_count) AS mention_count
				FROM greenroom.user_graph_nodes gn
				JOIN auth.users u
				  ON u.id = gn.user_id
				WHERE u.is_deleted = false
				  AND u.is_active = true
				GROUP BY gn.label, gn.grp
				ORDER BY mention_count DESC, weight DESC, gn.label ASC
				""",
			(rs, rowNum) -> new GraphNodeResponse(
				rs.getString("label"),
				rs.getString("grp"),
				null,
				rs.getDouble("weight"),
				rs.getInt("mention_count"),
				null,
				null,
				null
			)
		);
	}

	public List<GraphEdgeResponse> findOrganizationLinks() {
		return jdbcTemplate.query(
			"""
				SELECT
				    ge.source_label,
				    ge.target_label,
				    SUM(ge.weight) AS weight
				FROM greenroom.user_graph_edges ge
				JOIN auth.users u
				  ON u.id = ge.user_id
				WHERE u.is_deleted = false
				  AND u.is_active = true
				GROUP BY
				    ge.source_label,
				    ge.source_grp,
				    ge.target_label,
				    ge.target_grp
				ORDER BY weight DESC, ge.source_label ASC, ge.target_label ASC
				""",
			(rs, rowNum) -> new GraphEdgeResponse(
				rs.getString("source_label"),
				null,
				rs.getString("target_label"),
				null,
				rs.getInt("weight"),
				null,
				null,
				null
			)
		);
	}

	public Map<String, Long> findOrganizationCategoryDistribution() {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(
			"""
				SELECT
				    gn.grp,
				    SUM(gn.mention_count) AS mention_count
				FROM greenroom.user_graph_nodes gn
				JOIN auth.users u
				  ON u.id = gn.user_id
				WHERE u.is_deleted = false
				  AND u.is_active = true
				GROUP BY gn.grp
				ORDER BY mention_count DESC, gn.grp ASC
				"""
		);

		Map<String, Long> result = new LinkedHashMap<>();
		for (Map<String, Object> row : rows) {
			result.put((String) row.get("grp"), ((Number) row.get("mention_count")).longValue());
		}
		return result;
	}
}
