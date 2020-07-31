const listContainer = document.querySelector('#service-list');
let servicesRequest = new Request('/service');
fetch(servicesRequest)
.then(function(response) { return response.json(); })
.then(function(serviceList) {
  serviceList.forEach(service => {
    let li = document.createElement("li");
    let deleteButton = document.createElement("button");
    deleteButton.value = "Delete!";
    deleteButton.id = service.id;
    deleteButton.appendChild(document.createTextNode("Delete"));
      deleteButton.onclick = evt => {
          fetch('/service/' + service.id, {
              method: 'delete',
              headers: {
                  'Accept': 'application/json, text/plain, */*',
                  'Content-Type': 'application/json'
              }
          }).then(res => location.reload());
      }
    li.appendChild(document.createTextNode(service.name + ' (' + service.url + '): ' + service.status));
    li.appendChild(deleteButton);
    li.title = service.addedOn;
    listContainer.appendChild(li);
  });
});

const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {
    let url = document.querySelector('#url').value;
    let name = document.querySelector('#name').value;
    fetch('/service', {
    method: 'post',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    },
  body: JSON.stringify({url:url, name:name})
}).then(res=> location.reload());
}