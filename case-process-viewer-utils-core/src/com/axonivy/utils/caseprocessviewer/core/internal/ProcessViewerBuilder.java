package com.axonivy.utils.caseprocessviewer.core.internal;

import static com.axonivy.utils.caseprocessviewer.core.constants.CaseProcessViewerConstants.AND;
import static com.axonivy.utils.caseprocessviewer.core.constants.CaseProcessViewerConstants.SLASH;
import static com.axonivy.utils.caseprocessviewer.core.enums.ViewerParam.APP;
import static com.axonivy.utils.caseprocessviewer.core.enums.ViewerParam.CASE_PROCESS_VIEWER;
import static com.axonivy.utils.caseprocessviewer.core.enums.ViewerParam.CASE_PROCESS_VIEWER_FILE;
import static com.axonivy.utils.caseprocessviewer.core.enums.ViewerParam.FACES;
import static com.axonivy.utils.caseprocessviewer.core.enums.ViewerParam.FILE;
import static com.axonivy.utils.caseprocessviewer.core.enums.ViewerParam.HIGHLIGHT;
import static com.axonivy.utils.caseprocessviewer.core.enums.ViewerParam.PMV;
import static com.axonivy.utils.caseprocessviewer.core.enums.ViewerParam.SELECT;
import static com.axonivy.utils.caseprocessviewer.core.enums.ViewerParam.SERVER;
import static com.axonivy.utils.caseprocessviewer.core.enums.ViewerParam.VIEW;
import static com.axonivy.utils.caseprocessviewer.core.enums.ViewerParam.ZOOM;

import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.utils.caseprocessviewer.core.enums.ViewerParam;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.htmldialog.IHtmlDialogContext;
import ch.ivyteam.ivy.security.ISecurityContext;

public class ProcessViewerBuilder {

  private static final String PARAM_TEMPLATE = "{%s}";
  private final Map<ViewerParam, String> queryParams = new HashMap<>();
  private final String contextPath;

  public ProcessViewerBuilder() {
    IApplication application = IApplication.current();
    contextPath = application.getContextPath();
    setQueryParam(SERVER, detectServerParam());
    setQueryParam(APP, application.getName());
  }

  @SuppressWarnings("deprecation")
  private String detectServerParam() {
    String server = IHtmlDialogContext.current().applicationHomeLink().toAbsoluteUri().getAuthority();;
    String securityContextName = ISecurityContext.current().getName();
    if (!ISecurityContext.DEFAULT.equals(securityContextName)) {
      server = StringUtils.join(server, SLASH, securityContextName);
    }
    return server;
  }

  public ProcessViewerBuilder pmv(String pmvName) {
    return setQueryParam(PMV, pmvName);
  }

  public ProcessViewerBuilder projectPath(String projectRelativePath) {
    return setQueryParam(FILE, projectRelativePath);
  }

  public ProcessViewerBuilder highlight(String elementId) {
    return addQueryParam(HIGHLIGHT, elementId);
  }

  public ProcessViewerBuilder select(String elementIds) {
    return addQueryParam(SELECT, elementIds);
  }

  public ProcessViewerBuilder zoom(int zoom) {
    return setQueryParam(ZOOM, String.valueOf(zoom));
  }

  public URI toURI() {
    var uriBuilder = UriBuilder.fromPath(contextPath)
        .path(FACES.getValue())
        .path(VIEW.getValue())
        .path(CASE_PROCESS_VIEWER.getValue())
        .path(CASE_PROCESS_VIEWER_FILE.getValue());
    // Build URI with template e.g /uri/param={param}
    List<String> queryParamKeys = queryParams.keySet().stream()
        .sorted(Comparator.comparingInt(ViewerParam::ordinal))
        .map(ViewerParam::getValue).toList();
    for (var queryParam : queryParamKeys) {
      uriBuilder = uriBuilder.queryParam(queryParam, PARAM_TEMPLATE.formatted(queryParam));
    }
    return uriBuilder.buildFromMap(queryParams.entrySet().stream()
        .collect(Collectors.toMap(entry -> entry.getKey().getValue(), Map.Entry::getValue)));
  }

  private ProcessViewerBuilder setQueryParam(ViewerParam param, String value) {
    queryParams.put(param, value);
    return this;
  }

  private ProcessViewerBuilder addQueryParam(ViewerParam param, String value) {
    queryParams.compute(param, (key, val) -> {
      return StringUtils.isBlank(val) ? value : val.concat(AND).concat(value);
    });
    return this;
  }

}
