<script>
    function updatePerson(event, id) {
    event.preventDefault();
    const formData = new FormData(document.getElementById('updateForm'));
    fetch(`/people/${id}`, {
        method: 'PATCH',
        body: formData
}).then(response => {
    if (response.ok) {
    window.location.href = "/people"; // Redirect after successful update
} else {
    return response.text().then(text => { // Get error message
    alert('Error updating person: ' + text);
});
}
}).catch(error => {
    alert('Error updating person: ' + error.message);
});
}
</script>
