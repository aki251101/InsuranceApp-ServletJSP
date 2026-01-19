<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<jsp:include page="WEB-INF/views/common/header.jsp">
    <jsp:param name="title" value="ホーム" />
    <jsp:param name="active" value="" />
</jsp:include>

<div class="row">
    <div class="col-md-12">
        <h1 class="mb-4">損保管理システム</h1>
        <p class="lead">自動車保険の契約管理と事故対応を効率化するシステムです。</p>
    </div>
</div>

<div class="row mt-4">
    <div class="col-md-6">
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">契約管理</h5>
                <p class="card-text">契約の更新・解約管理、早期更改率の確認ができます。</p>
                <a href="${pageContext.request.contextPath}/policies" class="btn btn-primary">契約一覧へ</a>
            </div>
        </div>
    </div>
    <div class="col-md-6">
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">事故管理</h5>
                <p class="card-text">事故の受付・対応状況の管理、滞留チェックができます。</p>
                <a href="${pageContext.request.contextPath}/accidents" class="btn btn-primary">事故一覧へ</a>
            </div>
        </div>
    </div>
</div>

<jsp:include page="WEB-INF/views/common/footer.jsp" />