<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="jp.insurance.system.util.DateUtil" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="事故詳細" />
    <jsp:param name="active" value="accidents" />
</jsp:include>

<h2 class="mb-4">事故詳細</h2>

<!-- メッセージ表示 -->
<c:if test="${not empty message}">
    <div class="alert alert-${messageType} alert-dismissible fade show" role="alert">
            ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<div class="card mb-4">
    <div class="card-header">
        <h5 class="mb-0">基本情報</h5>
    </div>
    <div class="card-body">
        <table class="table table-borderless">
            <tbody>
            <tr>
                <th width="150">事故受付日</th>
                <td>${DateUtil.formatDate(accident.occurredAt)}</td>
            </tr>
            <tr>
                <th>場所</th>
                <td>${accident.place}</td>
            </tr>
            <tr>
                <th>概要</th>
                <td>${accident.description}</td>
            </tr>
            <tr>
                <th>ステータス</th>
                <td>
                    ${accident.status.displayName}
                    <c:if test="${accidentService.isStagnant(accident)}">
                        <span class="badge bg-danger ms-2">滞留</span>
                    </c:if>
                </td>
            </tr>
            <tr>
                <th>最終対応日時</th>
                <td>
                    <c:choose>
                        <c:when test="${accident.lastContactedAt != null}">
                            ${accident.lastContactedAt.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))}
                        </c:when>
                        <c:otherwise>
                            未対応
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
            <tr>
                <th>関連契約</th>
                <td>
                    <a href="${pageContext.request.contextPath}/policies/detail?id=${accident.policyId}">
                        ${accident.policyNumber} - ${accident.customerName}
                    </a>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>

<!-- メモ -->
<div class="card mb-4">
    <div class="card-header">
        <h5 class="mb-0">対応メモ</h5>
    </div>
    <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/accidents/memo">
            <input type="hidden" name="id" value="${accident.id}">
            <div class="mb-3">
                <textarea class="form-control" name="memo" rows="5">${accident.memo}</textarea>
            </div>
            <button type="submit" class="btn btn-primary">メモを保存</button>
        </form>
    </div>
</div>

<!-- 操作ボタン -->
<div class="card">
    <div class="card-header">
        <h5 class="mb-0">操作</h5>
    </div>
    <div class="card-body">
        <!-- 対応開始ボタン -->
        <c:if test="${accident.status.name() == 'OPEN'}">
            <form method="post" action="${pageContext.request.contextPath}/accidents/status" style="display:inline;">
                <input type="hidden" name="id" value="${accident.id}">
                <input type="hidden" name="status" value="IN_PROGRESS">
                <button type="submit" class="btn btn-primary">対応開始</button>
            </form>
        </c:if>

        <!-- 対応したボタン（対応履歴の更新） -->
        <c:if test="${accident.status.name() != 'RESOLVED'}">
            <form method="post" action="${pageContext.request.contextPath}/accidents/contacted" style="display:inline;">
                <input type="hidden" name="id" value="${accident.id}">
                <button type="submit" class="btn btn-info">対応した</button>
            </form>
        </c:if>

        <!-- 解決ボタン（事故対応がすべて終わった状態へ） -->
        <c:if test="${accident.status.name() == 'IN_PROGRESS'}">
            <form method="post" action="${pageContext.request.contextPath}/accidents/status" style="display:inline;">
                <input type="hidden" name="id" value="${accident.id}">
                <input type="hidden" name="status" value="RESOLVED">
                <button type="submit" class="btn btn-success"
                        onclick="return confirm('解決にしますか？解決後は戻せません。')">解決</button>
            </form>
        </c:if>
    </div>
</div>

<div class="mt-3">
    <a href="${pageContext.request.contextPath}/accidents" class="btn btn-outline-secondary">一覧に戻る</a>
</div>

<jsp:include page="../common/footer.jsp" />