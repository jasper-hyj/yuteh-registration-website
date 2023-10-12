'usestrict';
function getExcel() {
    var select = document.getElementById("form_select");
    var link = document.getElementById("excel_link");
    link.href = `/admin/excel/download?id=${select.value}`
}
function setAttributes(el, attrs) {
    for (var key in attrs) {
        el.setAttribute(key, attrs[key]);
    }
}
function searchData() {
    const XHR = new XMLHttpRequest();
    // Bind the FormData object and the form element
    const FD = new FormData(searchForm);
    searchFieldset.disabled = true;
    var object = {};
    FD.forEach((value, key) => {
        // Reflect.has in favor of: object.hasOwnProperty(key)
        if (!Reflect.has(object, key)) {
            object[key] = value;
            return;
        }
        if (!Array.isArray(object[key])) {
            object[key] = [object[key]];
        }
        object[key].push(value);
    });

    var json = JSON.stringify(object);
    // Define what happens on successful data submission
    XHR.addEventListener("load", function (event) {
        var dataList = JSON.parse(event.target.responseText);
        var table = document.getElementById("searchResultTable");
        table.textContent = '';
        var resultCount = document.getElementById("resultCount");
        resultCount.innerText = `共${Object.keys(dataList).length}筆資料`;
        for (var index in dataList) {
            var tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${dataList[index]["createTime"]}</td>
                <td>${dataList[index]["studentId"]}</td>
                <td>${dataList[index]["studentBirthDate"]}</td>
                <td>${dataList[index]["studentChineseName"]}</td>
                <td>${dataList[index]["mark"]}</td>
                <td>
                <button type="button" class="btn btn-warning btn-sm" data-bs-toggle="modal" data-bs-target="#modal${dataList[index]["UUID"]}">
                詳細資訊
                </button></td>`;
            table.appendChild(tr);
            var modalDiv = document.createElement("div");
            setAttributes(modalDiv, {
                "class": "modal fade",
                "id": `modal${dataList[index]["UUID"]}`,
                "data-bs-backdrop": `static`,
                "data-bs-keyboard": "false",
                "tabindex": "-1",
                "aria-hidden": "true"
            })
            var dataForm = "";
            dataForm +=
                `
                    <div class="modal-dialog  modal-lg">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="staticBackdropLabel">詳細資料</h4>
                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>
                            <div class="modal-body">
                            
                            `
            var dataJSON = dataList[index]["dataJSON"]
            var key_list = Object.keys(dataJSON);
            key_list.shift();
            key_list.forEach((key, index) => {
                var sectionJson = dataJSON[key];
                var columnList = Object.keys(sectionJson);
                var sectionTable = `<div><h3 class="fw-bold mt-3 ms-2">${key}</h3><table class="table"><tbody>`
                columnList.forEach((columnKey, columnIndex) => {
                    // console.log(columnKey + ": " + sectionJson[columnKey]);
                    sectionTable += `<tr><td style="width: 50%;">${columnKey}</td><td>${JSON.stringify(sectionJson[columnKey]).replace(/\"/g, "")}</td></tr>`;
                })
                sectionTable += "</tbody></table></div>";
                dataForm += sectionTable;
            })
            dataForm += `</div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-warning" data-bs-dismiss="modal">關閉</button>
                </div></div></div>`
            modalDiv.innerHTML = dataForm;
            table.appendChild(modalDiv);
        }
        searchFieldset.disabled = false;

    });

    // Define what happens in case of error
    XHR.addEventListener("error", function (event) {
        alert(event.target.responseText);
        searchFieldset.disabled = false;
    });
    // Set up our request
    XHR.open("POST", "/admin/search");

    // The data sent is what the user provided in the form
    XHR.send(json);
}

var searchForm = document.getElementById("searchForm");
var searchFieldset = document.getElementById("searchFieldset")
if (searchForm != null) {
    searchForm.addEventListener("submit", function (event) {
        event.preventDefault();
        searchData();
    });
}