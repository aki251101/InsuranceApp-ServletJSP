<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="jp.insurance.system.util.DateUtil" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="契約一覧" />
    <jsp:param name="active" value="policies" />
</jsp:include>

<h2 class="mb-4">契約一覧</h2>

<!-- 集計表示 -->
<div class="row mb-4">
    <div class="col-md-6">
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">早期更改率（当年度）</h5>
                <p class="card-text display-6">${fiscalStats.formattedRate}</p>
            </div>
        </div>
    </div>
    <div class="col-md-6">
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">早期更改率（当月）</h5>
                <p class="card-text display-6">${monthlyStats.formattedRate}</p>
            </div>
        </div>
    </div>
</div>

<!-- 検索フォーム -->
<form method="get" action="${pageContext.request.contextPath}/policies" class="mb-3">
    <div class="input-group">
        <input type="text" name="q" class="form-control" placeholder="契約番号または契約者名で検索" value="${query}">
        <button type="submit" class="btn btn-outline-secondary">検索</button>
    </div>
</form>

<!-- タブ -->
<ul class="nav nav-tabs mb-3">
    <li class="nav-item">
        <a class="nav-link ${currentTab == 'renewable' ? 'active' : ''}" 
           href="${pageContext.request.contextPath}/policies?tab=renewable">更新可能契約</a>
    </li>
    <li class="nav-item">
        <a class="nav-link ${currentTab == 'active' ? 'active' : ''}" 
           href="${pageContext.request.contextPath}/policies?tab=active">契約中</a>
    </li>
    <li class="nav-item">
        <a class="nav-link ${currentTab == 'cancelled' ? 'active' : ''}" 
           href="${pageContext.request.contextPath}/policies?tab=cancelled">解約</a>
    </li>
    <li class="nav-item">
        <a class="nav-link ${currentTab == 'lapsed' ? 'active' : ''}" 
           href="${pageContext.request.contextPath}/policies?tab=lapsed">失効</a>
    </li>
    <li class="nav-item">
        <a class="nav-link ${currentTab == 'all' ? 'active' : ''}" 
           href="${pageContext.request.contextPath}/policies?tab=all">全件</a>
    </li>
</ul>

<!-- 新規登録ボタン -->
<div class="mb-3">
    <a href="${pageContext.request.contextPath}/policies/new" class="btn btn-success">新規契約登録</a>
</div>

<!-- 一覧表 -->
<c:if test="${empty policies}">
    <div class="alert alert-info">該当する契約がありません。</div>
</c:if>

<c:if test="${not empty policies}">
    <table class="table table-hover">
        <thead class="table-light">
            <tr>
                <th>満期日</th>
                <th>契約番号</th>
                <th>契約者名</th>
                <th>ステータス</th>
                <th>要注意</th>
                <th></th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="policy" items="${policies}">
                <c:set var="today" value="<%= DateUtil.today() %>" />
                <tr>
                    <td>${DateUtil.formatDate(policy.endDate)}</td>
                    <td>${policy.policyNumber}</td>
                    <td>${policy.customerName}</td>
                    <td>${policyService.getDisplayStatus(policy)}</td>
                    <td>
                        <c:if test="${policyService.isAttentionPeriod(policy, today)}">
                            <span class="badge bg-warning text-dark">要注意</span>
                        </c:if>
                    </td>
                    <td>
                        <a href="${pageContext.request.contextPath}/policies/detail?id=${policy.id}" 
                           class="btn btn-sm btn-primary">詳細</a>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</c:if>

<jsp:include page="../common/footer.jsp" />