function Button({ text, count, setCount }) {


  async function handleClick() {
  const url = "http://localhost:8082/debate/Banany%20hodne%20smrdi";
  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Response status: ${response.status}`);
    }

    const result = await response.json();
    console.log(result);
    setCount(result.title);
  } catch (error) {
    console.error(error.message);
  }
}



  return (
    <button onClick={handleClick}>{text} + {count}</button>
  );
}