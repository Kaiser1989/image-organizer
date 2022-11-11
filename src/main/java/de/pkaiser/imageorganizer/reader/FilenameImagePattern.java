package de.pkaiser.imageorganizer.reader;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FilenameImagePattern {

	private final Pattern pattern;
	
	private final int dateGroup;
		
	private final DateTimeFormatter dateFormatter;
	
	public FilenameImagePattern(final String pattern, final int dateGroup, final String dateFormat) {
		this.pattern = Pattern.compile(pattern);
		this.dateGroup = dateGroup;
		this.dateFormatter = new DateTimeFormatterBuilder().appendPattern(dateFormat)
				.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
				.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
				.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
				.parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
				.toFormatter().withZone(ZoneId.systemDefault());
	}
	
	public Optional<Instant> tryParse(final String filename) {
		final Matcher matcher = this.pattern.matcher(filename);
		if (matcher.matches()) {
			final String dateString = matcher.group(dateGroup);
			try {
				return Optional.of(dateFormatter.parse(dateString, Instant::from));	
			}  catch (DateTimeParseException e) {
				log.error(String.format("Failed to parse date string: %s", dateString), e);
			}
		}
		return Optional.empty();
	}
}
