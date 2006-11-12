package org.codejive.web.weblog;

import java.util.Date;

import org.codejive.common.CodejiveException;

public interface Dated {
	Date getDate() throws CodejiveException;
}
