package cobone.controllers;

import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cobone.model.ActionLog;
import cobone.model.ActionLog.Action;
import cobone.model.helper.DailyCount;
import cobone.model.helper.Series;
import cobone.repo.ActionLogRepo;

@RestController
@RequestMapping("/logger")
public class LoggerController {

	@Autowired
	private ActionLogRepo actionLogRepo;
	@Autowired
	private ObjectMapper objectMapper;

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> logUIActions(@RequestBody ActionLog actionLog) {
		actionLogRepo.save(actionLog);
		return ResponseEntity.ok().build();
	}

	@RequestMapping(method = RequestMethod.GET, path = "/stats/all")
	public ResponseEntity<?> getAllActions() {
		List<DailyCount> list = actionLogRepo.getAllActions();
		return ResponseEntity.ok(optimizeForCharts(list, EnumSet.allOf(Action.class), true));
	}

	@RequestMapping(method = RequestMethod.GET, path = "/stats/referrer/{action}")
	public ResponseEntity<?> getAllActivites(@PathVariable("action") Action action) {
		List<DailyCount> list = actionLogRepo.getReferrerByAction(action);
		return ResponseEntity.ok(optimizeForCharts(list, actionLogRepo.findDistinctReferrer(), false));
	}

	/// ------------------------------ private --------------------------------

	private void fillAndSortEntry(Entry<Date, List<DailyCount>> entry, Collection<?> allList) {
		allList.removeAll(entry.getValue().stream().map(o -> o.getAction()).collect(toList()));
		// fill zeros
		allList.forEach(a -> entry.getValue().add(new DailyCount(entry.getKey(), a, 0l)));
	}

	private ObjectNode optimizeForCharts(List<DailyCount> list, Collection<?> allList,
			boolean compareAndSort /*
									 * TODO in a fancy world, one should
									 * encapsulate behavior using interfaces
									 */) {

		Map<Date, List<DailyCount>> collect = list.stream().collect(groupingBy(DailyCount::getDay));
		collect.entrySet().stream().forEach(e -> fillAndSortEntry(e, allList));

		Map<Object, List<Long>> collect2 = collect.values().stream().flatMap(o -> o.stream())
				.collect(groupingBy(DailyCount::getAction, mapping(DailyCount::getCount, toList())));

		List<Series> series = collect2.entrySet().stream().map(e -> new Series(e.getKey().toString(), e.getValue()))
				.collect(Collectors.toList());

		if (compareAndSort) {
			series.sort(Comparator.comparing(s -> Action.forName(s.getName().toString())));
			series.forEach(s -> s.setName(Action.forName(s.getName().toString()).getName()));
		}

		ObjectNode root = objectMapper.createObjectNode();
		root.putPOJO("categories", collect.keySet());
		root.putPOJO("series", series);

		return root;
	}
}