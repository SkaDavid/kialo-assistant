import { useState } from 'react';

const ArgumentForm = ({ onSubmit, onCancel, initialData}) => {
    const [ formData, setFormData ] = useState(initialData);

    return (
        <div className="argument-form" onClick={(e) => e.stopPropagation()}>
            <input type="text" placeholder="Text" onChange={(e) => setFormData({ ...formData, text: e.target.value })} value={formData.text}/>
            <div>
            <label>
                <input type="radio" name="argType" value="PRO" checked={formData.type === "PRO"} onChange={(e) => {setFormData({ ...formData, type: e.target.value })}} ></input>
                Pro
            </label>

            <label>
                <input type="radio" name="argType" value="CON" checked={formData.type === "CON"} onChange={(e) => {setFormData({ ...formData, type: e.target.value })}}></input>
                Con
            </label>
            </div>
            <button onClick={() => onSubmit(formData)}>Send</button>
            <button onClick={onCancel}>Cancel</button>
        </div>
    )
}

export default ArgumentForm;