<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="契約新規登録" />
    <jsp:param name="active" value="policies" />
</jsp:include>

<h2 class="mb-4">契約新規登録</h2>

<!-- エラーメッセージ -->
<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        ${errorMessage}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<div class="card">
    <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/policies/new">

            <div class="mb-3">
                <label class="form-label">契約番号</label>
                <input type="text" class="form-control" value="（登録時に自動採番されます）" disabled>
            </div>

            <div class="mb-3">
                <label for="customerName" class="form-label">契約者名 <span class="text-danger">*</span></label>
                <input type="text" class="form-control" id="customerName" name="customerName"
                       value="${customerName}" required>
            </div>

            <div class="mb-3">
                <label for="startDate" class="form-label">開始日 <span class="text-danger">*</span></label>
                <input type="date" class="form-control" id="startDate" name="startDate"
                       value="${startDate}" required>
            </div>

            <div class="mb-3">
                <label for="endDate" class="form-label">満期日（自動計算：開始日の1年後）</label>
                <input type="date" class="form-control" id="endDate" name="endDate"
                       value="${endDate}" readonly>
            </div>

            <div class="mt-4">
                <button type="submit" class="btn btn-primary">登録</button>
                <a href="${pageContext.request.contextPath}/policies" class="btn btn-secondary">キャンセル</a>
            </div>
        </form>
    </div>
</div>

<script>
document.getElementById('startDate').addEventListener('change', function() {
    var startDateStr = this.value;
    if (!startDateStr) {
        document.getElementById('endDate').value = '';
        return;
    }

    // 開始日をパース
    var parts = startDateStr.split('-');
    var year = parseInt(parts[0], 10);
    var month = parseInt(parts[1], 10);
    var day = parseInt(parts[2], 10);

    // 1年後を計算
    var endYear = year + 1;
    var endMonth = month;
    var endDay = day;

    // 2月29日 → 翌年が平年なら2月28日
    if (month === 2 && day === 29) {
        if (!isLeapYear(endYear)) {
            endDay = 28;
        }
    }

    // 満期日をセット
    var endDateStr = endYear + '-' +
                     String(endMonth).padStart(2, '0') + '-' +
                     String(endDay).padStart(2, '0');
    document.getElementById('endDate').value = endDateStr;
});

function isLeapYear(year) {
    return (year % 4 === 0 && year % 100 !== 0) || (year % 400 === 0);
}
</script>

<jsp:include page="../common/footer.jsp" />